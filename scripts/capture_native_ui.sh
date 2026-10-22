#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

: "${ANDROID_SERIAL:?Set ANDROID_SERIAL to an isolated emulator}"
: "${ANDROID_HOME:?Set ANDROID_HOME to the Android SDK directory}"
case "$ANDROID_SERIAL" in
  emulator-*) ;;
  *) echo "Use an isolated emulator for fixture captures." >&2; exit 1 ;;
esac
adb_bin="$ANDROID_HOME/platform-tools/adb"
runner="org.zotero.android.library.LibraryTestRunner"
destination="${1:-docs/ui/native}"

if [[ "${NATIVE_UI_SKIP_BUILD:-0}" != 1 ]]; then
  bash gradlew --no-configuration-cache --max-workers=1 "-PuiTestRunner=$runner" \
    :app:assembleDevDebug :app:assembleDevDebugAndroidTest
fi
"$adb_bin" -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/dev/debug/app-dev-debug.apk
"$adb_bin" -s "$ANDROID_SERIAL" install -r app/build/outputs/apk/androidTest/dev/debug/app-dev-debug-androidTest.apk

restore_display() {
  "$adb_bin" -s "$ANDROID_SERIAL" shell wm size reset >/dev/null
  "$adb_bin" -s "$ANDROID_SERIAL" shell wm density reset >/dev/null
}
trap restore_display EXIT

capture() {
  local mode="$1" size="$2" density="$3" classes="$4"
  mkdir -p "$destination/$mode"
  "$adb_bin" -s "$ANDROID_SERIAL" shell wm size "$size"
  "$adb_bin" -s "$ANDROID_SERIAL" shell wm density "$density"
  "$adb_bin" -s "$ANDROID_SERIAL" shell am instrument -w -e class "$classes" \
    "org.zotero.android.debug.test/$runner" > "$destination/$mode/instrumentation.txt"
  cat "$destination/$mode/instrumentation.txt"
  if ! grep -Eq '^OK \([0-9]+ tests?\)' "$destination/$mode/instrumentation.txt"; then
    echo "Fixture tests failed; inspect $destination/$mode/instrumentation.txt" >&2
    return 1
  fi
  "$adb_bin" -s "$ANDROID_SERIAL" exec-out run-as org.zotero.android.debug \
    tar -cf - -C files/ui-screenshots . > "$destination/$mode/screenshots.tar"
  tar -xf "$destination/$mode/screenshots.tar" -C "$destination/$mode"
  rm "$destination/$mode/screenshots.tar"
}

capture phone 1080x2340 440 \
  org.zotero.android.library.LibraryInteractionTest,org.zotero.android.library.MobileUiTest,org.zotero.android.library.ItemCoverTest,org.zotero.android.library.LibraryScreenshotTest,org.zotero.android.library.AdaptiveLibraryTest
capture unfolded 1680x1800 320 org.zotero.android.library.AdaptiveLibraryTest
capture landscape 1920x1400 320 org.zotero.android.library.AdaptiveLibraryTest
