package org.zotero.android.library

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.util.concurrent.atomic.AtomicInteger
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.zotero.android.BuildConfig
import org.zotero.android.appupdate.*
import org.zotero.android.uicomponents.library.LibraryTheme
import java.io.File
import java.security.MessageDigest

class AppUpdateTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun repository(code: Int = 404, body: String = "") = UpdateRepository(context,
        UpdateManifestClient(OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(code)
                .message("Test").body(body.toResponseBody()).build()
        }.build()), Gson())

    @Test fun noReleaseAndNetworkFailuresHaveDifferentStates() = runBlocking {
        context.getSharedPreferences("app_updates", Context.MODE_PRIVATE).edit().clear().commit()
        val repository = repository()
        assertTrue(repository.state.value.automatic)
        assertTrue(repository.state.value.wifiOnly)
        assertTrue(repository.check())
        assertEquals(UpdateStatus.NO_RELEASE, repository.state.value.status)
        assertTrue(repository.state.value.lastChecked > 0)
        val unavailable = repository(503)
        assertFalse(unavailable.check())
        assertEquals(UpdateError.NETWORK, unavailable.state.value.error)
    }

    @Test fun concurrentChecksAreCoalesced() = runBlocking {
        context.getSharedPreferences("app_updates", Context.MODE_PRIVATE).edit().clear().commit()
        val requests = AtomicInteger()
        val client = UpdateManifestClient(OkHttpClient.Builder().addInterceptor { chain ->
            requests.incrementAndGet()
            Thread.sleep(100)
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(404)
                .message("Missing").body("".toResponseBody()).build()
        }.build())
        val repository = UpdateRepository(context, client, Gson())
        listOf(async { repository.check() }, async { repository.check() }).awaitAll()
        assertEquals(1, requests.get())
    }

    @Test fun automaticChecksIgnoreLegacyOptOut() {
        val preferences = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
        preferences.edit().clear().putBoolean("automatic", false).commit()
        assertTrue(repository().state.value.automatic)
        preferences.edit().clear().commit()
    }

    @Test fun automaticCheckNeverStartsADownload(): Unit = runBlocking {
        val prefs = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val manifest = AppUpdateManifest(BuildConfig.VERSION_CODE.toLong() + 1, "1.0.0-next", context.packageName,
            "devDebug", 23, 1024, "0".repeat(64),
            "https://github.com/nicholashyang/zotero-android/releases/download/test/app.apk", "Update fixture")
        val client = UpdateManifestClient(OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200).message("OK")
                .body(Gson().toJson(manifest).toResponseBody()).build()
        }.build())
        val repository = UpdateRepository(context, client, Gson())
        assertTrue(repository.check(automatic = true))
        assertEquals(UpdateStatus.AVAILABLE, repository.state.value.status)
        assertEquals(-1L, prefs.getLong("downloadId", -1))
        prefs.edit().clear().commit()
    }

    @Test fun invalidApkIsNeverHandedToInstaller() {
        val file = File(context.cacheDir, "corrupt-update.apk").apply { writeText("not an APK") }
        val hash = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
        val manifest = AppUpdateManifest(BuildConfig.VERSION_CODE.toLong() + 1, "1.0.0-next", context.packageName,
            "devDebug", 23, file.length(), hash,
            "https://github.com/nicholashyang/zotero-android/releases/download/test/app.apk", "")
        assertThrows(Exception::class.java) { repository().verifyApk(file, manifest) }
        assertThrows(Exception::class.java) { repository().verifyApk(file, manifest.copy(sha256 = "0".repeat(64))) }
        assertThrows(Exception::class.java) { repository().verifyApk(file, manifest.copy(applicationId = "other.app")) }
        file.delete()
    }

    @Test fun checkingDisablesRepeatedChecks() {
        compose.setContent { LibraryTheme { UpdateSection(AppUpdateState(status = UpdateStatus.CHECKING)) } }
        compose.onNodeWithText("Check for Updates").assertIsNotEnabled()
    }

    @Test fun readyUpdateOffersInstallation() {
        var installs = 0
        compose.setContent {
            LibraryTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    UpdateSection(AppUpdateState(status = UpdateStatus.READY), onInstall = { installs++ })
                }
            }
        }
        compose.onNodeWithText("Install Update").performScrollTo().assertIsEnabled().performClick()
        assertEquals(1, installs)
    }
}
