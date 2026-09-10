package org.zotero.android.appupdate

import org.junit.Assert.*
import org.junit.Test

class AppUpdateManifestTest {
    private fun manifest(overrides: Map<String, Any> = emptyMap()): String {
        val values = linkedMapOf<String, Any>(
            "versionCode" to 281, "versionName" to "1.0.0-281",
            "applicationId" to "org.zotero.android.debug", "channel" to "devDebug",
            "minSdk" to 23, "sizeBytes" to 100, "sha256" to "a".repeat(64),
            "apkUrl" to "https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.0.0-281/Zotero-dev-debug.apk",
            "releaseNotes" to "Update notes",
        )
        values.putAll(overrides)
        return com.google.gson.Gson().toJson(values)
    }

    @Test fun onlyIncreasingBuildsAreUpdates() {
        val version = AppUpdateManifest.parse(manifest())
        assertTrue(version.isNewerThan(280))
        assertFalse(version.isNewerThan(281))
        assertFalse(version.isNewerThan(1000))
    }

    @Test fun displayVersionDoesNotDetermineUpgradeOrder() {
        assertTrue(AppUpdateManifest.parse(manifest(mapOf("versionName" to "0.9.0-281"))).isNewerThan(280))
    }

    @Test fun rejectsWrongPackageChannelAndAndroidVersion() {
        val update = AppUpdateManifest.parse(manifest())
        assertTrue(update.isCompatible("org.zotero.android.debug", 23))
        assertFalse(update.isCompatible("org.zotero.android", 36))
        assertFalse(update.isCompatible("org.zotero.android.debug", 22))
        assertFalse(AppUpdateManifest.parse(manifest(mapOf("channel" to "internalRelease")))
            .isCompatible("org.zotero.android.debug", 36))
    }

    @Test fun rejectsUntrustedAndMutableDownloadLocations() {
        listOf(
            "http://github.com/nicholashyang/zotero-android/releases/download/v281/app.apk",
            "https://github.com/attacker/repo/releases/download/v281/app.apk",
            "https://github.com/nicholashyang/zotero-android/releases/latest/download/app.apk",
            "https://github.com.evil.test/nicholashyang/zotero-android/releases/download/v281/app.apk",
            "https://name@github.com/nicholashyang/zotero-android/releases/download/v281/app.apk",
            "https://github.com/nicholashyang/zotero-android/releases/download/v281/app.apk?redirect=1",
            "https://github.com/nicholashyang/zotero-android/releases/download/../app.apk",
        ).forEach { url -> assertThrows(Exception::class.java) { AppUpdateManifest.parse(manifest(mapOf("apkUrl" to url))) } }
    }

    @Test fun rejectsMissingMalformedAndOversizedData() {
        listOf("{}", "null", "[]", "not json", " ".repeat(262145)).forEach {
            assertThrows(Exception::class.java) { AppUpdateManifest.parse(it) }
        }
        listOf("versionCode" to 0, "versionCode" to 281.5, "versionCode" to "281",
            "minSdk" to -1, "sizeBytes" to 0, "sha256" to "invalid", "versionName" to "")
            .forEach { (key, value) -> assertThrows(Exception::class.java) { AppUpdateManifest.parse(manifest(mapOf(key to value))) } }
    }

    @Test fun normalizesHashForVerification() {
        assertEquals("a".repeat(64), AppUpdateManifest.parse(manifest(mapOf("sha256" to "A".repeat(64)))).sha256)
    }
}
