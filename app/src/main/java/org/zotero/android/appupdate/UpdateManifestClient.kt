package org.zotero.android.appupdate

import okhttp3.OkHttpClient
import okhttp3.Request
import org.zotero.android.api.annotations.ForNonZoteroApi
import java.io.IOException
import javax.inject.Inject

/** A missing release is distinct from a failed request or a malformed manifest. */
class UpdateManifestClient @Inject constructor(@ForNonZoteroApi private val client: OkHttpClient) {
    fun fetch(): AppUpdateManifest? {
        client.newCall(Request.Builder().url(AppUpdateManifest.MANIFEST_URL).build()).execute().use { response ->
            if (response.code == 404) return null
            if (!response.isSuccessful) throw IOException("Update HTTP ${response.code}")
            val json = response.body.charStream().use { reader ->
                val buffer = CharArray(262145)
                var count = 0
                while (count < buffer.size) {
                    val read = reader.read(buffer, count, buffer.size - count)
                    if (read == -1) break
                    count += read
                }
                require(count <= 262144)
                String(buffer, 0, count)
            }
            return AppUpdateManifest.parse(json)
        }
    }
}
