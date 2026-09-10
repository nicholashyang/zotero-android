# Development updates

The `devDebug` app (`org.zotero.android.debug`) checks the public Releases in
`nicholashyang/zotero-android`. It retains the existing development certificate.
Install the first updater-enabled APK over the previous development installation;
subsequent updates can be downloaded from Settings → Software Update.

Automatic checking is enabled by default, approximately once every 24 hours.
Android may delay background work. New versions produce a notification (when permitted) and an in-app reminder. Downloads
start only after a user action and ask before using a metered connection. Installation
always starts with a user action and the Android package installer. On Android 8+
the app can request permission to install packages; on Android 13+ notification
permission is optional. The update screen remains available without notifications.
Update preferences are device preferences and survive signing out of Zotero.

## Release procedure

1. Keep `.github/workflows/release-dev.yml` registered on the default branch.
2. Run **Publish development update** from GitHub Actions. Select the source ref
   (default `codex/ios-inspired-ui`), base version, increasing Android version code,
   and notes. Inputs are passed through environment variables, never evaluated as
   shell commands. The checkout commit is pinned for the rest of the run.
3. The workflow downloads the previous publicly released APK and builds the release candidate, runs unit
   tests and Lint, and runs instrumented tests plus a data-retention upgrade check
   on API 23, 26, 33 and 36. A failed matrix job blocks publication.
4. It creates `dev-v<base>-<code>` as a draft, uploads `Zotero-dev-debug.apk` and
   `update-dev.json`, then publishes and marks it latest. Do not mark these releases
   as GitHub prereleases: the app uses GitHub's `/releases/latest/download/` route.
5. Verify the public manifest and the downloaded APK checksum after publication.

A draft reserves its build number. If publication fails, inspect the draft and
workflow logs; do not overwrite assets of an already published version. Publish a
higher build number to replace a faulty release. The updater never downgrades.
The upstream Google Play workflow only runs in the upstream repository.

## Update manifest

`scripts/create_update_manifest.py` extracts `applicationId`, `versionCode`,
`versionName`, and `minSdk` from the actual APK using `aapt`. It adds `channel`
(`devDebug`), `sizeBytes`, `sha256`, `releaseNotes`, and `apkUrl`. The URL is pinned
to this repository's specific release tag. The client rejects unknown package or
channel, unsupported Android versions, malformed fields and non-HTTPS/untrusted
APK locations. Downloaded bytes, version, package and signing certificate must
match before a file is passed to the installer. Installed certificates remain the
local trust anchor; the system installer performs final package verification.

The app does not use a GitHub token. HTTP 404 is shown as no published update;
network/server failures and invalid manifests are reported separately. Download
IDs and manifests are persisted; WorkManager and download broadcasts reconcile
completed tasks after process loss. APKs are copied into private internal storage
for verification and installation. Successful updates clear previous cached APKs.

## Local checks

Use the toolchain described in DEVELOPMENT.md. Run:

```sh
bash gradlew --no-configuration-cache --max-workers=1 \
  -PuiTestRunner=org.zotero.android.library.LibraryTestRunner \
  :app:assembleDevDebug :app:testDevDebugUnitTest :app:lintDevDebug \
  :app:assembleDevDebugAndroidTest
```

On an isolated emulator, `scripts/test_update_device.sh` accepts a previous APK,
the new APK, and its instrumentation APK. Set `ANDROID_HOME` and `ANDROID_SERIAL`.
The fixture runner uses a plain Application so component tests cannot sign in or
sync. Live production download and installer checks must use the normal app
entry point and a separate empty emulator installation.
