package org.zotero.android.appupdate

import com.google.gson.JsonParser
import java.net.URI

/** The public release contract. APK URLs are pinned to a release, never to /latest. */
data class AppUpdateManifest(
    val versionCode: Long,
    val versionName: String,
    val applicationId: String,
    val channel: String,
    val minSdk: Int,
    val sizeBytes: Long,
    val sha256: String,
    val apkUrl: String,
    val releaseNotes: String,
) {
    fun isNewerThan(installedVersion: Long) = versionCode > installedVersion

    fun isCompatible(packageName: String, sdk: Int) =
        applicationId == packageName && channel == "devDebug" && minSdk <= sdk

    companion object {
        const val REPOSITORY = "nicholashyang/zotero-android"
        const val MANIFEST_URL = "https://github.com/$REPOSITORY/releases/latest/download/update-dev.json"

        fun parse(json: String): AppUpdateManifest {
            require(json.length <= 262144) { "Update manifest is too large" }
            val root = JsonParser.parseString(json).asJsonObject
            fun string(key: String): String {
                val value = root.get(key)
                require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isString)
                return value.asString
            }
            fun number(key: String): Long {
                val value = root.get(key)
                require(value != null && value.isJsonPrimitive && value.asJsonPrimitive.isNumber)
                return value.asBigDecimal.longValueExact()
            }
            val version = number("versionCode")
            val minSdk = number("minSdk")
            val size = number("sizeBytes")
            val hash = string("sha256")
            val url = string("apkUrl")
            val uri = URI(url)
            require(version in 1..2100000000L && minSdk in 1..1000 && size in 1..2147483648L)
            require(hash.matches(Regex("[a-fA-F0-9]{64}")))
            require(uri.scheme == "https" && uri.host == "github.com" && uri.port == -1)
            require(uri.userInfo == null && uri.query == null && uri.fragment == null)
            require(!uri.rawPath.split("/").any { it == "." || it == ".." })
            require(uri.rawPath.matches(Regex("/$REPOSITORY/releases/download/[A-Za-z0-9._-]+/[A-Za-z0-9._-]+\\.apk")))
            val name = string("versionName")
            require(name.isNotBlank() && name.length <= 100)
            return AppUpdateManifest(version, name, string("applicationId"), string("channel"),
                minSdk.toInt(), size, hash.lowercase(), url, string("releaseNotes").take(16000))
        }
    }
}

enum class UpdateStatus {
    IDLE, CHECKING, UP_TO_DATE, NO_RELEASE, AVAILABLE, WAITING_FOR_NETWORK,
    DOWNLOADING, VERIFYING, READY, ERROR,
}

enum class UpdateError { NETWORK, MANIFEST, INCOMPATIBLE, DOWNLOAD, SPACE, VERIFICATION, INSTALL }

data class AppUpdateState(
    val status: UpdateStatus = UpdateStatus.IDLE,
    val manifest: AppUpdateManifest? = null,
    val automatic: Boolean = true,
    val wifiOnly: Boolean = true,
    val lastChecked: Long = 0,
    val downloadedBytes: Long = 0,
    val error: UpdateError? = null,
) {
    val busy: Boolean get() = status in setOf(UpdateStatus.CHECKING, UpdateStatus.DOWNLOADING,
        UpdateStatus.WAITING_FOR_NETWORK, UpdateStatus.VERIFYING)
}
