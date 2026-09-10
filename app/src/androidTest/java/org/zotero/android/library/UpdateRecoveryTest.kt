package org.zotero.android.library

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.zotero.android.BuildConfig
import org.zotero.android.appupdate.*

/** Opt-in, isolated emulator checks with Wi-Fi and mobile data switched off. */
class UpdateRecoveryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val preferences get() = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
    private val downloads get() = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    private val id get() = preferences.getLong("downloadId", -1)
    private val ownedIds = mutableSetOf<Long>()
    private val manifest = AppUpdateManifest(
        BuildConfig.VERSION_CODE.toLong() + 1, "1.0.0-next", "org.zotero.android.debug",
        "devDebug", 23, 1024, "0".repeat(64),
        "https://github.com/nicholashyang/zotero-android/releases/download/offline-test/test.apk", "",
    )

    private fun repository() = UpdateRepository(context, UpdateManifestClient(
        OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200)
                .message("Fixture").body(Gson().toJson(manifest).toResponseBody()).build()
        }.build()), Gson())

    @Suppress("DEPRECATION")
    @Before fun requireOfflineEmulator() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runOfflineUpdateTests") == "true")
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        assertFalse("Disable both test-emulator networks first", connectivity.activeNetworkInfo?.isConnected == true)
        assertEquals("No existing download may be replaced by a test", -1L, id)
        preferences.edit().clear().commit()
    }

    @After fun cleanTestDownloads() {
        ownedIds.forEach { downloads.remove(it) }
        if (ownedIds.isNotEmpty()) preferences.edit().clear().commit()
    }

    private suspend fun queued(automatic: Boolean = false): UpdateRepository {
        val repository = repository()
        assertTrue(repository.check(automatic))
        if (!automatic) repository.download()
        assertTrue("Download must be persisted", id >= 0)
        ownedIds.add(id)
        return repository
    }

    @Test fun pendingDownloadSurvivesRecreationWithoutDuplication(): Unit = runBlocking {
        queued()
        val original = id
        val restored = repository()
        restored.check()
        restored.download()
        assertEquals(original, id)
        withTimeout(30_000) {
            while (restored.state.value.status != UpdateStatus.WAITING_FOR_NETWORK) {
                delay(500)
                restored.refresh()
                assertNotEquals(UpdateStatus.ERROR, restored.state.value.status)
            }
        }
        assertEquals(original, id)
    }

    @Test fun turningOffAutomaticCancelsAutomaticDownload(): Unit = runBlocking {
        val repository = queued(automatic = true)
        val original = id
        repository.setAutomatic(false)
        withTimeout(10_000) { while (id != -1L) delay(50) }
        downloads.query(DownloadManager.Query().setFilterById(original)).use { assertFalse(it.moveToFirst()) }
        assertEquals(UpdateStatus.AVAILABLE, repository.state.value.status)
    }

    @Test fun turningOffAutomaticPreservesManualDownload(): Unit = runBlocking {
        val repository = queued()
        val original = id
        repository.setAutomatic(false)
        delay(500)
        repository.refresh()
        assertEquals(original, id)
        downloads.query(DownloadManager.Query().setFilterById(original)).use { assertTrue(it.moveToFirst()) }
        assertFalse(repository().state.value.automatic)
    }

    @Test fun removedDownloadReportsFailureAndCanBeRetried(): Unit = runBlocking {
        val repository = queued()
        val original = id
        downloads.remove(original)
        repository.refresh()
        assertEquals(UpdateError.DOWNLOAD, repository.state.value.error)
        assertEquals(-1L, id)
        repository.download()
        assertTrue(id >= 0)
        assertNotEquals(original, id)
        ownedIds.add(id)
    }
}
