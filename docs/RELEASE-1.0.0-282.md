# Release 1.0.0-282 acceptance

This release updates the mobile sign-in, library home, preferences, gestures and offline math rendering. Implementation notes and verification boundaries are in [Mobile interface update](ui/MOBILE-UI.md).

## Local acceptance

- Development APK built with version code 282 and the existing development certificate.
- 71 JVM tests passed with no failures or skips.
- The API 35 phone fixture passed 40 UI/update tests and four offline DownloadManager recovery tests.
- The actual public 281 APK was installed and upgraded to the candidate 282 APK. The private data marker survived.
- The tablet-size API 35 fixture passed all 10 mobile UI checks after the legacy WebView compatibility adjustment.
- Both legacy automatic-download cancellation and preservation of manual transfers passed.
- Screenshots verified that offline MathJax emits SVG and does not duplicate assistive text. The gesture gutter is tested independently from card actions.
- Lint ran successfully and retains the existing baseline of 571 errors and one fatal finding. New feature classes did not introduce error/fatal findings; this is not a clean-Lint claim.

The published workflow is required to pass the same device checks on API 23, 26, 33 and 36 before publishing. Public artifact and workflow results are appended after publication.

Actual account authentication, first synchronization, authenticated library switching, vendor password-manager behavior and full screen-reader interaction remain outside fixture validation. No real account database was used for the tests.
