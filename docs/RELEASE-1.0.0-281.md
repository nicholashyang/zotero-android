# Release 1.0.0-281 acceptance

Published at `2026-09-10T17:45:11Z` from commit `f84f68c232230253642f312322ad720fe34aa725`.

- [Release](https://github.com/nicholashyang/zotero-android/releases/tag/dev-v1.0.0-281)
- [APK](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.0.0-281/Zotero-dev-debug.apk)
- [Update manifest](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.0.0-281/update-dev.json)
- [Successful release workflow](https://github.com/nicholashyang/zotero-android/actions/runs/34508611608)

## Public artifact verification

The public latest-manifest route and pinned APK URL were downloaded independently after publication.
The release tag resolves to the exact build commit. Actual APK metadata, size, SHA-256 and the existing development signing certificate match the manifest.

| Field | Verified value |
| --- | --- |
| Version | 1.0.0-281 |
| Version code | 281 |
| Package | org.zotero.android.debug |
| Channel | devDebug |
| Minimum API | 23 |
| Bytes | 182813699 |
| SHA-256 | `9ff6e61b58c0730e5b4c08e336fe40c0656c1bc698134506518853d234237bc4` |
| Signing certificate SHA-256 | `c4e70a6935d239246540532b6d3ea514eb2a428e1e8eaacbf94c8a9a01989299` |

## Automated and device checks

- Development build and 66 JVM tests passed.
- API 23, 26, 33 and 36 each passed the 280-to-281 data-retention upgrade check, 29 UI/component checks and four real DownloadManager recovery checks.
- The older Android search-focus issue found during acceptance was fixed. CI uses 4 GB emulator storage and 2 GB RAM. Test orchestration uses root only after the app UI checks for artifact export and network controls; application instrumentation still runs under the app UID.
- Final CI Lint has 571 errors and one fatal finding already present in the baseline; no new errors. Warnings decreased from 695 to 692. This is not a clean Lint baseline.
- English/Chinese update resources, light/dark mode, large fonts and phone/tablet layouts were checked.

## Production update flow (API 35 emulator)

- A bootstrap build 280 read the published manifest, downloaded the public APK through DownloadManager, verified it and offered installation. The opt-in production integration test passed.
- The actual system installer upgraded that installation to the public 281 APK. Two private data markers and the verified-download preference survived. The obsolete private APK was removed on the next app start.
- Update preferences survived a full process restart. Metered manual download prompted before starting; cancelling created no download task.
- Unknown-source installation permission was requested only on Install. Returning after granting permission resumed the system confirmation. Cancelling retained the verified package, including across process restart.
- The ready-to-install screen worked while notification permission was denied.
- A temporary low-storage condition produced the space error without enqueuing a download; the reserved storage was removed afterward.

The pre-updater build 280 needs one manual overwrite installation of this release. Do not uninstall to upgrade; subsequent releases can be installed through Software Update.
