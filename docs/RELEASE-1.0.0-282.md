# Release 1.0.0-282 acceptance

This release updates the mobile sign-in, library home, preferences, gestures and offline math rendering. Implementation notes and verification boundaries are in [Mobile interface update](ui/MOBILE-UI.md).

## Local acceptance

- Development APK built with version code 282 and the existing development certificate.
- 71 JVM tests passed with no failures or skips.
- The API 35 phone fixture passed 42 UI/update tests and four offline DownloadManager recovery tests.
- The actual public 281 APK was installed and upgraded to the candidate 282 APK. The private data marker survived.
- The tablet-size API 35 fixture passed all 12 mobile UI checks after the legacy WebView compatibility adjustment.
- Both legacy automatic-download cancellation and preservation of manual transfers passed.
- Screenshots verified that offline MathJax emits SVG and does not duplicate assistive text. The gesture gutter is tested independently from card actions.
- Long detail formulas scroll horizontally, invalid TeX falls back to source, and a 200-row formula list scrolls with a bounded number of attached WebViews.
- The initial release attempt was correctly blocked on API 23. The compatibility fix transpiles all bundled MathJax dependencies to validated ES5, includes offline runtime polyfills, and uses Android's older screenshot API below API 26.
- Lint ran successfully and retains the existing baseline of 571 errors and one fatal finding. New feature classes did not introduce error/fatal findings; this is not a clean-Lint claim.

## Published release

- [Zotero 1.0.0-282](https://github.com/nicholashyang/zotero-android/releases/tag/dev-v1.0.0-282) is public, not a prerelease, and marked Latest.
- [APK](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.0.0-282/Zotero-dev-debug.apk) and [update manifest](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.0.0-282/update-dev.json) are available publicly.
- The release tag resolves to the verified source [5dd05f162d09f5899a7404fe72f20b8e490fc9a4](https://github.com/nicholashyang/zotero-android/commit/5dd05f162d09f5899a7404fe72f20b8e490fc9a4).
- [Publish workflow 34534679624](https://github.com/nicholashyang/zotero-android/actions/runs/34534679624) passed build, JVM tests, all four device jobs and publication. Its `release-validation` and `device-results-api-*` artifacts contain the underlying reports.

| Android API | UI / update tests | Offline recovery tests | Public 281 upgrade marker |
| --- | --- | --- | --- |
| 23 | 42 passed | 4 passed | Retained |
| 26 | 42 passed | 4 passed | Retained |
| 33 | 42 passed | 4 passed | Retained |
| 36 | 42 passed | 4 passed | Retained |

The original API 23 WebView passed actual SVG DOM rendering, wide-formula scrolling, invalid-TeX fallback and 200-row scrolling checks. The reviewed phone and tablet visual captures use API 35; API 23's immediate screen captures do not reliably include WebView compositor output.

The public APK was downloaded again through its public URL, independently of the workflow artifacts. Its metadata, signing certificate, length and SHA-256 match the public Latest update manifest:

| Field | Verified value |
| --- | --- |
| Display name | Zotero |
| Package | `org.zotero.android.debug` |
| Version | `1.0.0-282`, code `282` |
| Minimum SDK | 23 |
| Bytes | 183832720 |
| APK SHA-256 | `e973ef0eb3c32ac125b186fab5e1343e5100ed7cb65811d3cc4202fa1679be08` |
| Certificate SHA-256 | `c4e70a6935d239246540532b6d3ea514eb2a428e1e8eaacbf94c8a9a01989299` |

The public 281 application discovered 282 through the real Latest endpoint and downloaded the APK only after an explicit download call. Its own size/hash/package/version/signature checks accepted the file. The isolated emulator required its stale proxy to be removed and a GitHub-only localhost TLS tunnel because direct emulator networking was unreliable. The tunnel did not intercept TLS, change responses or bypass application verification.

The final [live update test](validation/update-live-282.txt) passed in 32.495 seconds, including scrolling through the full release notes to the Install Update button. The test helper now scrolls to that button and allows up to three transient network-check attempts. These post-release test/documentation changes do not change the APK's pinned source commit. [Machine-readable verification](validation/release-282.json) records the artifact and matrix results.

The independently downloaded public APK was then installed over public 281 on the API 35 emulator. The installed version is 282 and the private data marker remains intact.

Lint execution completed with 571 errors, one fatal finding and 705 warnings in the existing project baseline. This does not represent a clean-Lint release.

Actual account authentication, first synchronization, authenticated library switching, vendor password-manager behavior and full screen-reader interaction remain outside fixture validation. No real account database was used for the tests.
