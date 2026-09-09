# Development builds

This branch builds the existing `devDebug` variant, with application ID
`org.zotero.android.debug`, the existing debug signing key, name and icon.
Release signing and PDF SDK configuration retain their upstream behavior.

## Prerequisites

- JDK 17 (set `JAVA_HOME` to the JDK home).
- Android SDK: platform `android-37.0`, build tools `36.0.0`, platform tools.
- Set `ANDROID_HOME` to the SDK directory, or set `sdk.dir` in an untracked
  `local.properties` file.
- Python 3 and Bash for the existing asset bundlers.
- Internet access for the first Gradle/dependency download and initialized Git
  submodules. Keep the toolchain in a persistent directory, not `/tmp`.

```sh
git clone --recurse-submodules https://github.com/nicholashyang/zotero-android.git
cd zotero-android
git switch codex/ios-inspired-ui
git submodule update --init --recursive
bash scripts/prepare_dev_assets.sh
bash gradlew --no-configuration-cache --max-workers=1 \
  :app:assembleDevDebug :app:testDevDebugUnitTest :app:lintDevDebug
```

The branch must be published to the remote before another checkout can use the
`git switch` command above. In the original workspace it already exists locally.

The asset step bundles translators, translation code, citation processing,
styles, CSL locales, utilities, PDF worker and the HTML/EPUB reader. A successful
Kotlin build alone does not guarantee those assets are present.

APK: `app/build/outputs/apk/dev/debug/app-dev-debug.apk`.
Reports: `app/build/reports/tests/` and
`app/build/reports/lint-results-devDebug.html`.

The checkout includes `app/google-services.json`. If developers remove it and
have no variant-specific replacement, only the dev/debug Google Services and
Crashlytics processing tasks are skipped. Do not add release keys to the
repository. `pspdfkit-key.txt` remains optional for the existing null-key SDK
initialization path; production PDF functionality needs its own license review.

The upstream Lint configuration has `abortOnError = false`; a successful Lint
task means a report was generated, not that the repository is free of errors.
The verification report records the baseline and introduced findings separately.
Use one worker for Lint: a concurrent local run stalled in Kotlin PSI traversal,
while sequential runs completed.

## Isolated Compose tests and screenshots

Use an empty emulator. These tests install the development package and substitute
a plain `Application` only in the instrumentation process. They do not log in,
open the user's database, start sync or initialize the PDF SDK. The normal app
entry point still uses `ZoteroApplication`.

```sh
bash gradlew --no-configuration-cache --max-workers=1 \
  -PuiTestRunner=org.zotero.android.library.LibraryTestRunner \
  :app:connectedDevDebugAndroidTest
```

To retain and export screenshots, use the helper with an explicit emulator serial:

```sh
ANDROID_SERIAL=emulator-5554 bash scripts/capture_library_ui.sh docs/ui/ui-screenshots
```

The helper builds and installs both APKs, runs the fixture tests and exports
Compose-root PNGs. Gradle's connected-test cleanup may uninstall the app, so the
helper uses `am instrument` for capture. Images are production-component fixtures,
not screenshots of an authenticated library. See [UI validation](ui/README.md).

## Development CI

`.github/workflows/dev.yml` initializes submodules, prepares assets, runs the
development build, JVM tests and Lint, then uploads APKs and reports. It runs for
pull requests, `codex/**` pushes and manual dispatch. It does not publish a release.
Local emulator results are documented separately from CI.
