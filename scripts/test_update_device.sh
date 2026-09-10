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
network_changed=false
finish_tests() {
  test_exit_code=$?
  if [ "$test_exit_code" -ne 0 ]; then
    "$adb_bin" -s "$serial" logcat -d -t 400 > device-results/failure-logcat.txt || true
  fi
  if "$network_changed"; then
    "$adb_bin" -s "$serial" shell svc wifi enable || true
    "$adb_bin" -s "$serial" shell svc data enable || true
  fi
  return "$test_exit_code"
}
trap finish_tests EXIT
install_apk() {
  # Old PackageManager versions can report failure while adb exits successfully.
  "$adb_bin" -s "$serial" install -r "$1" | tee device-results/install.txt
  grep -q '^Success' device-results/install.txt
}
install_apk "$previous_apk"
"$adb_bin" -s "$serial" shell "run-as org.zotero.android.debug sh -c 'echo retained > files-upgrade-marker'"
install_apk "$new_apk"
expected_code=$("$ANDROID_HOME/build-tools/36.0.0/aapt" dump badging "$new_apk" | sed -n "s/^package:.*versionCode='\([0-9]*\)'.*/\1/p")
"$adb_bin" -s "$serial" shell dumpsys package org.zotero.android.debug > device-results/package.txt
grep -q "versionCode=$expected_code " device-results/package.txt
"$adb_bin" -s "$serial" shell run-as org.zotero.android.debug cat files-upgrade-marker > device-results/upgrade-marker.txt
grep -q retained device-results/upgrade-marker.txt
install_apk "$test_apk"
"$adb_bin" -s "$serial" shell am instrument -w \
  -e class org.zotero.android.library.AppUpdateTest,org.zotero.android.library.LibraryInteractionTest,org.zotero.android.library.LibraryScreenshotTest \
  org.zotero.android.debug.test/org.zotero.android.library.LibraryTestRunner | tee device-results/instrumentation.txt
grep -qE '^OK \([0-9]+ tests?\)' device-results/instrumentation.txt
# Root is limited to test orchestration on this isolated emulator. Instrumentation
# still runs as the application UID, including the offline recovery tests below.
"$adb_bin" -s "$serial" root
"$adb_bin" -s "$serial" wait-for-device
test "$("$adb_bin" -s "$serial" shell id -u | tr -d '\r')" = 0
"$adb_bin" -s "$serial" shell dumpsys package org.zotero.android.debug > device-results/package.txt
echo 'Exporting verified UI captures'
"$adb_bin" -s "$serial" shell tar -cf /data/local/tmp/zotero-update-screenshots.tar \
  -C /data/data/org.zotero.android.debug/files/ui-screenshots .
"$adb_bin" -s "$serial" pull /data/local/tmp/zotero-update-screenshots.tar device-results/screenshots.tar
tar -tf device-results/screenshots.tar > device-results/screenshot-files.txt
"$adb_bin" -s "$serial" shell rm -f /data/local/tmp/zotero-update-screenshots.tar

# Exercise the real system download queue while no transfer can leave the emulator.
network_changed=true
echo 'Disabling emulator Wi-Fi'
"$adb_bin" -s "$serial" shell svc wifi disable
echo 'Disabling emulator mobile data'
"$adb_bin" -s "$serial" shell svc data disable
echo 'Checking offline download recovery'
"$adb_bin" -s "$serial" shell am instrument -w \
  -e class org.zotero.android.library.UpdateRecoveryTest -e runOfflineUpdateTests true \
  org.zotero.android.debug.test/org.zotero.android.library.LibraryTestRunner | tee device-results/recovery.txt
grep -q '^OK (4 tests)' device-results/recovery.txt
