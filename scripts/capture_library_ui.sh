#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

: "${ANDROID_SERIAL:?Set ANDROID_SERIAL to an isolated emulator (for example emulator-5554)}"
: "${ANDROID_HOME:?Set ANDROID_HOME to the Android SDK directory}"
case "$ANDROID_SERIAL" in
  emulator-*) ;;
  *) echo "Use an isolated emulator; this script installs the development app." >&2; exit 1 ;;
esac

destination="${1:-docs/ui/ui-screenshots}"
adb_bin="$ANDROID_HOME/platform-tools/adb"
runner="org.zotero.android.library.LibraryTestRunner"
bash gradlew --no-configuration-cache --max-workers=1 "-PuiTestRunner=$runner" \
  :app:assembleDevDebug :app:assembleDevDebugAndroidTest
"$adb_bin" -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/dev/debug/app-dev-debug.apk
"$adb_bin" -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/androidTest/dev/debug/app-dev-debug-androidTest.apk

mkdir -p "$destination"
test_log="$destination/instrumentation.txt"
"$adb_bin" -s "$ANDROID_SERIAL" shell am instrument -w \
  -e class org.zotero.android.library.LibraryInteractionTest,org.zotero.android.library.LibraryScreenshotTest \
  "org.zotero.android.debug.test/$runner" | tee "$test_log"
# am instrument may return exit status 0 for a test failure.
if ! grep -Eq '^OK \([0-9]+ tests?\)' "$test_log"; then
  echo "Instrumentation did not report success; see $test_log" >&2
  exit 1
fi
archive="$(mktemp)"
trap 'rm -f "$archive"' EXIT
"$adb_bin" -s "$ANDROID_SERIAL" exec-out run-as org.zotero.android.debug \
  tar -cf - -C files/ui-screenshots . > "$archive"
tar -xf "$archive" -C "$destination"
