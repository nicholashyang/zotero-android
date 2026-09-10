package org.zotero.android.appupdate

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.net.toUri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.core.content.edit
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.zotero.android.BuildConfig
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/** One owner for foreground UI, periodic checks and download-completion reconciliation. */
@Singleton
class UpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val manifestClient: UpdateManifestClient,
    private val gson: Gson,
) {
    private val preferences = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
    private val downloads = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutableState = MutableStateFlow(AppUpdateState(
        manifest = preferences.getString("manifest", null)?.let { runCatching { AppUpdateManifest.parse(it) }.getOrNull() },
        automatic = preferences.getBoolean("automatic", true),
        wifiOnly = preferences.getBoolean("wifiOnly", true),
        lastChecked = preferences.getLong("lastChecked", 0),
    ))
    val state = mutableState.asStateFlow()
    private val internalDirectory get() = File(context.filesDir, "app-updates").apply { mkdirs() }
    private val readyFile get() = File(internalDirectory, "update.apk")
    private val downloadId get() = preferences.getLong("downloadId", -1)

    fun start() {
        if (!BuildConfig.SELF_UPDATE_ENABLED) return
        UpdateScheduler.schedule(context, state.value.automatic)
        scope.launch { refresh() }
    }

    fun setAutomatic(enabled: Boolean) {
        preferences.edit { putBoolean("automatic", enabled) }
        mutableState.update { it.copy(automatic = enabled) }
        UpdateScheduler.schedule(context, enabled)
        scope.launch {
            mutex.withLock {
                if (!state.value.automatic && preferences.getBoolean("automaticDownload", false)) {
                    removeDownload()
                    publish(if (state.value.manifest != null) UpdateStatus.AVAILABLE else UpdateStatus.IDLE)
                }
            }
        }
    }

    fun setWifiOnly(enabled: Boolean) {
        preferences.edit { putBoolean("wifiOnly", enabled) }
        mutableState.update { it.copy(wifiOnly = enabled) }
        scope.launch {
            mutex.withLock {
                if (downloadId != -1L && preferences.getBoolean("automaticDownload", false)) {
                    removeDownload()
                    if (state.value.automatic) enqueueDownload(automatic = true)
                    else publish(UpdateStatus.AVAILABLE)
                }
            }
        }
    }

    fun isMetered(): Boolean =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).isActiveNetworkMetered

    @Suppress("DEPRECATION")
    private fun downloadNetworkAvailable(): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivity.activeNetworkInfo?.isConnected != true) return false
        return !preferences.getBoolean("automaticDownload", false) || !state.value.wifiOnly || !isMetered()
    }

    /** Returns false only for transient check failures, so WorkManager can back off. */
    suspend fun check(automatic: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!BuildConfig.SELF_UPDATE_ENABLED || (automatic && !state.value.automatic)) return@withLock true
            reconcile()
            if (downloadId != -1L || state.value.status == UpdateStatus.READY) return@withLock true
            // Coalesce a foreground check racing the periodic worker or another tap.
            if (System.currentTimeMillis() - state.value.lastChecked < 1000 &&
                state.value.status in setOf(UpdateStatus.NO_RELEASE, UpdateStatus.UP_TO_DATE, UpdateStatus.AVAILABLE) &&
                (!automatic || state.value.status != UpdateStatus.AVAILABLE)) return@withLock true
            publish(UpdateStatus.CHECKING)
            try {
                val manifest = manifestClient.fetch()
                if (manifest == null) {
                    saveManifest(null)
                    checked()
                    publish(UpdateStatus.NO_RELEASE)
                    return@withLock true
                }
                require(manifest.isCompatible(context.packageName, Build.VERSION.SDK_INT)) { "incompatible" }
                checked()
                if (!manifest.isNewerThan(BuildConfig.VERSION_CODE.toLong())) {
                    saveManifest(null)
                    publish(UpdateStatus.UP_TO_DATE)
                } else {
                    saveManifest(manifest)
                    publish(UpdateStatus.AVAILABLE)
                    if (automatic && state.value.automatic) enqueueDownload(automatic = true)
                }
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                fail(UpdateError.NETWORK)
                false
            } catch (e: Exception) {
                fail(if (e.message == "incompatible") UpdateError.INCOMPATIBLE else UpdateError.MANIFEST)
                true
            }
        }
    }

    suspend fun download() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (!BuildConfig.SELF_UPDATE_ENABLED) return@withLock
            reconcile()
            if (downloadId == -1L && state.value.status != UpdateStatus.READY) enqueueDownload(automatic = false)
        }
    }

    suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock { if (BuildConfig.SELF_UPDATE_ENABLED) reconcile() }
    }

    private fun enqueueDownload(automatic: Boolean) {
        val manifest = state.value.manifest ?: return
        if (!manifest.isNewerThan(BuildConfig.VERSION_CODE.toLong()) ||
            !manifest.isCompatible(context.packageName, Build.VERSION.SDK_INT)) {
            fail(UpdateError.INCOMPATIBLE)
            return
        }
        try {
            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: throw IOException("Download storage unavailable"), "app-updates").apply { mkdirs() }
            if (StatFs(directory.path).availableBytes < manifest.sizeBytes * 2 + 32 * 1024 * 1024) {
                fail(UpdateError.SPACE)
                return
            }
            directory.listFiles()?.forEach { it.delete() }
            val request = DownloadManager.Request(manifest.apkUrl.toUri())
                .setTitle("Zotero ${manifest.versionName}")
                .setMimeType("application/vnd.android.package-archive")
                .setAllowedOverMetered(!automatic || !state.value.wifiOnly)
                .setAllowedOverRoaming(!automatic)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,
                    "app-updates/${manifest.versionCode}.apk")
            val id = downloads.enqueue(request)
            preferences.edit(commit = true) {
                putLong("downloadId", id)
                putBoolean("automaticDownload", automatic)
                remove("readyVersion")
            }
            publish(if (downloadNetworkAvailable()) UpdateStatus.DOWNLOADING else UpdateStatus.WAITING_FOR_NETWORK)
            UpdateScheduler.reconcileLater(context)
        } catch (e: Exception) {
            fail(UpdateError.DOWNLOAD)
        }
    }

    private fun reconcile() {
        val manifest = state.value.manifest
        if (manifest == null || !manifest.isNewerThan(BuildConfig.VERSION_CODE.toLong())) {
            removeDownload()
            readyFile.delete()
            preferences.edit { remove("readyVersion") }
            UpdateNotifications.cancel(context)
            if (manifest != null) {
                saveManifest(null)
                publish(UpdateStatus.UP_TO_DATE)
            }
            return
        }
        if (preferences.getLong("readyVersion", -1) == manifest.versionCode && readyFile.isFile) {
            publish(UpdateStatus.READY)
            return
        }
        val id = downloadId
        if (id == -1L) {
            if (state.value.status == UpdateStatus.IDLE || state.value.status == UpdateStatus.READY) publish(UpdateStatus.AVAILABLE)
            return
        }
        try {
            downloads.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
                if (cursor == null || !cursor.moveToFirst()) {
                    removeDownload()
                    fail(UpdateError.DOWNLOAD)
                    return
                }
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val bytes = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                mutableState.update { it.copy(downloadedBytes = bytes) }
                when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        publish(UpdateStatus.VERIFYING)
                        val staging = File(internalDirectory, "update.tmp")
                        try {
                            if (StatFs(internalDirectory.path).availableBytes < manifest.sizeBytes + 16 * 1024 * 1024) {
                                throw UpdateStorageException()
                            }
                            downloads.openDownloadedFile(id).use { descriptor ->
                                android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { input ->
                                    staging.outputStream().use { output -> input.copyTo(output) }
                                }
                            }
                            verifyApk(staging, manifest)
                            readyFile.delete()
                            kotlin.check(staging.renameTo(readyFile))
                            preferences.edit(commit = true) { putLong("readyVersion", manifest.versionCode) }
                            removeDownload()
                            publish(UpdateStatus.READY)
                            UpdateNotifications.ready(context, manifest.versionName)
                        } catch (e: Exception) {
                            staging.delete()
                            removeDownload()
                            fail(if (e is UpdateStorageException) UpdateError.SPACE else UpdateError.VERIFICATION)
                        }
                    }
                    DownloadManager.STATUS_FAILED -> {
                        val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                        removeDownload()
                        fail(if (reason == DownloadManager.ERROR_INSUFFICIENT_SPACE) UpdateError.SPACE else UpdateError.DOWNLOAD)
                    }
                    DownloadManager.STATUS_PAUSED -> publish(UpdateStatus.WAITING_FOR_NETWORK)
                    // Pending requests may stay pending until connectivity returns, without a PAUSED transition.
                    else -> publish(if (downloadNetworkAvailable()) UpdateStatus.DOWNLOADING else UpdateStatus.WAITING_FOR_NETWORK)
                }
            }
        } catch (e: Exception) {
            fail(UpdateError.DOWNLOAD)
        }
    }

    /** Re-check immediately before granting another process read access. */
    suspend fun fileForInstall(): File? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val manifest = state.value.manifest ?: return@withLock null
            try {
                kotlin.check(BuildConfig.SELF_UPDATE_ENABLED)
                verifyApk(readyFile, manifest)
                readyFile
            } catch (e: Exception) {
                readyFile.delete()
                preferences.edit { remove("readyVersion") }
                fail(UpdateError.VERIFICATION)
                null
            }
        }
    }

    @Suppress("DEPRECATION")
    internal fun verifyApk(file: File, manifest: AppUpdateManifest) {
        require(manifest.isCompatible(context.packageName, Build.VERSION.SDK_INT))
        require(manifest.isNewerThan(BuildConfig.VERSION_CODE.toLong()))
        require(file.length() == manifest.sizeBytes)
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(65536)
            while (true) {
                val size = input.read(buffer)
                if (size == -1) break
                digest.update(buffer, 0, size)
            }
        }
        require(digest.digest().joinToString("") { "%02x".format(it) } == manifest.sha256)
        val flags = if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
        val archive = context.packageManager.getPackageArchiveInfo(file.path, flags) ?: error("Invalid APK")
        val installed = context.packageManager.getPackageInfo(context.packageName, flags)
        val code = if (Build.VERSION.SDK_INT >= 28) archive.longVersionCode else archive.versionCode.toLong()
        require(archive.packageName == context.packageName && code == manifest.versionCode && archive.versionName == manifest.versionName)
        if (Build.VERSION.SDK_INT >= 24) require(archive.applicationInfo?.minSdkVersion == manifest.minSdk)
        fun signatures(info: android.content.pm.PackageInfo): Set<String> {
            val signatures = if (Build.VERSION.SDK_INT >= 28) info.signingInfo?.apkContentsSigners else info.signatures
            return signatures.orEmpty().map { it.toCharsString() }.toSet()
        }
        require(signatures(installed).isNotEmpty() && signatures(installed) == signatures(archive))
    }

    fun installationFailed() = fail(UpdateError.INSTALL)

    private fun removeDownload() {
        val id = downloadId
        if (id != -1L) runCatching { downloads.remove(id) }
        preferences.edit(commit = true) { remove("downloadId"); remove("automaticDownload") }
    }

    private fun checked() {
        val now = System.currentTimeMillis()
        preferences.edit { putLong("lastChecked", now) }
        mutableState.update { it.copy(lastChecked = now) }
    }

    private fun saveManifest(manifest: AppUpdateManifest?) {
        preferences.edit(commit = true) { putString("manifest", manifest?.let(gson::toJson)) }
        mutableState.update { it.copy(manifest = manifest, downloadedBytes = 0) }
    }

    private fun publish(status: UpdateStatus) = mutableState.update { it.copy(status = status, error = null) }
    private fun fail(error: UpdateError) = mutableState.update { it.copy(status = UpdateStatus.ERROR, error = error) }
    private class UpdateStorageException : IOException()
}
