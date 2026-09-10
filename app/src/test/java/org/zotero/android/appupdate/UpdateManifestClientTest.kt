package org.zotero.android.appupdate

import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class UpdateManifestClientTest {
    private fun client(code: Int, body: String = "") = UpdateManifestClient(
        OkHttpClient.Builder().addInterceptor { chain ->
            assertEquals(AppUpdateManifest.MANIFEST_URL, chain.request().url.toString())
            assertNull(chain.request().header("Authorization"))
            Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                .code(code).message("Fixture").body(body.toResponseBody()).build()
        }.build())

    @Test fun missingReleaseIsNotAnError() { assertNull(client(404).fetch()) }
    @Test fun networkAndServerErrorsAreNotReportedAsUpToDate() {
        for (code in listOf(403, 429, 500, 503)) assertThrows(IOException::class.java) { client(code).fetch() }
        val offline = UpdateManifestClient(OkHttpClient.Builder().addInterceptor { throw IOException("offline") }.build())
        assertThrows(IOException::class.java) { offline.fetch() }
    }
    @Test fun invalidResponsesAreRejected() {
        for (body in listOf("{}", "<html>Proxy error</html>", "a".repeat(262145))) {
            assertThrows(Exception::class.java) { client(200, body).fetch() }
        }
    }
}
