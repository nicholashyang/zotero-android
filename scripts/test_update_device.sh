#!/usr/bin/env bash
# Runs only on an isolated emulator. Never clear or uninstall a user's installation.
set -euo pipefail
previous_apk="$1"
new_apk="$2"
test_apk="$3"
adb_bin="${ANDROID_HOME:?}/platform-tools/adb"
serial="${ANDROID_SERIAL:-emulator-${EMULATOR_PORT:-5554}}"
case "$serial" in emulator-*) ;; *) echo 'An isolated emulator is required' >&2; exit 1;; esac
mkdir -p device-results
"$adb_bin" -s "$serial" install -r "$previous_apk"
"$adb_bin" -s "$serial" shell "run-as org.zotero.android.debug sh -c 'echo retained > files-upgrade-marker'"
"$adb_bin" -s "$serial" install -r "$new_apk"
"$adb_bin" -s "$serial" shell run-as org.zotero.android.debug cat files-upgrade-marker > device-results/upgrade-marker.txt
grep -q retained device-results/upgrade-marker.txt
"$adb_bin" -s "$serial" install -r "$test_apk"
"$adb_bin" -s "$serial" shell am instrument -w \
  -e class org.zotero.android.library.AppUpdateTest,org.zotero.android.library.LibraryInteractionTest,org.zotero.android.library.LibraryScreenshotTest \
  org.zotero.android.debug.test/org.zotero.android.library.LibraryTestRunner | tee device-results/instrumentation.txt
grep -qE '^OK \([0-9]+ tests?\)' device-results/instrumentation.txt
"$adb_bin" -s "$serial" shell dumpsys package org.zotero.android.debug > device-results/package.txt
"$adb_bin" -s "$serial" exec-out run-as org.zotero.android.debug tar -cf - -C files/ui-screenshots . > device-results/screenshots.tar
