package org.zotero.android.library

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.zotero.android.BuildConfig
import org.zotero.android.appupdate.*

/** Run only after reserving temporary storage on an isolated emulator. */
class UpdateStorageTest {
    @Test fun insufficientSpaceDoesNotEnqueueDownload(): Unit = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runStorageUpdateTests") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val preferences = context.getSharedPreferences("app_updates", Context.MODE_PRIVATE)
        assertEquals(-1L, preferences.getLong("downloadId", -1))
        val bytes = 2_147_483_648L
        val directory = checkNotNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS))
        assertTrue("Reserve temporary emulator storage first", StatFs(directory.path).availableBytes < bytes * 2)
        val manifest = AppUpdateManifest(BuildConfig.VERSION_CODE.toLong() + 1, "1.0.0-next",
            context.packageName, "devDebug", 23, bytes, "0".repeat(64),
            "https://github.com/nicholashyang/zotero-android/releases/download/storage-test/test.apk", "")
        val client = UpdateManifestClient(OkHttpClient.Builder().addInterceptor { chain ->
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(200)
                .message("Fixture").body(Gson().toJson(manifest).toResponseBody()).build()
        }.build())
        try {
            preferences.edit().clear().commit()
            val repository = UpdateRepository(context, client, Gson())
            assertTrue(repository.check())
            repository.download()
            assertEquals(UpdateError.SPACE, repository.state.value.error)
            assertEquals(-1L, preferences.getLong("downloadId", -1))
        } finally {
            preferences.edit().clear().putBoolean("automatic", false).commit()
        }
    }
}
