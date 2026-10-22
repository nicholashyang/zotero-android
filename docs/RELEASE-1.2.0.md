# Zotero 1.2.0

Android build: `1.2.0-284`. Release tag: `dev-v1.2.0-284`.

## Changes

- One Apple-inspired visual system for the library, sign-in, onboarding,
  settings, detail pages and reader controls: blue actions, neutral grouped
  surfaces, clear title hierarchy, original stroke icons and dark appearance.
- Compact searchable library navigation, inset settings groups, redesigned
  sign-in and account forms, quieter bottom tools and readable detail fields.
- A single dashboard navigation graph across phone, multi-window and unfolded
  layouts, preserving current pages, search and restored list position.
- Collection sidebar on wide windows; Jetpack WindowManager fold information
  reserves separating hinges and keeps forms and controls in an unobstructed
  pane. Reader tool routes remain available through size changes.
- Native-style switches, scalable system typography and large-text support.

## Published release

- [Zotero 1.2.0-284](https://github.com/nicholashyang/zotero-android/releases/tag/dev-v1.2.0-284) is public, marked Latest and not a prerelease.
- Download the [APK](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.2.0-284/Zotero-dev-debug.apk) or inspect its [update manifest](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.2.0-284/update-dev.json).
- The release tag was checked against source commit [89fa5885c6de6af4d782bcb68577f1f1d3b4565c](https://github.com/nicholashyang/zotero-android/commit/89fa5885c6de6af4d782bcb68577f1f1d3b4565c). The [release workflow](https://github.com/nicholashyang/zotero-android/actions/runs/39856243141) passed build, all four device jobs and publication.
- The public APK's package metadata, original signing certificate, v1/v2 signatures, size and SHA-256 were verified. Both public asset digests match the downloaded files and the update manifest matches the APK. APK size: 183,897,106 bytes; SHA-256: `2752886e5fc8402aeb0ca81d5d9f1cacf8586a829dcab88a76e588f3e7be471b`.

| Android API | UI / update / adaptive tests | Offline recovery tests | 283-to-284 data marker |
| --- | --- | --- | --- |
| 23 | 58 passed | 4 passed | Retained |
| 26 | 58 passed | 4 passed | Retained |
| 33 | 58 passed | 4 passed | Retained |
| 36 | 58 passed | 4 passed | Retained |

API 36 additionally passed eight adaptive tests at the unfolded window size.
The CI JVM report records 84 tests with no failures or skips.

CI Lint retains 572 existing errors and reports 1,279 findings versus 1,278 in
1.1.0-283. The one new warning is an available WindowManager 1.5.1 update.
The [upstream patch](https://android.googlesource.com/platform/frameworks/support/+/4d5897960201a8713f0f77e0dbd4b0ec2009fff7)
changes rear-display APIs in `WindowAreaController`; the app calls
`WindowInfoTracker` for folds and does not call that rear-display API. The tested
1.5.0 pin is retained for this release, with the advisory recorded explicitly.
There are no new Lint errors.

[Machine-readable acceptance results](validation/release-1.2.0.json) record the
published artifact, source, checks and device matrix.

## Local verification

The final interface implementation built successfully and passed 84 JVM tests.
API 35 production-component fixtures passed 58 phone tests (including seven
update checks), eight unfolded tests and eight landscape tests. Four offline
update recovery tests also passed. The previous public `1.1.0-283` APK was
covered by `1.2.0-284` with the retained-data marker intact. A separating-hinge preview passed an additional
isolated repeat and was visually inspected, along with light/dark home pages,
settings, sign-in and detail fixtures.

Lint completed with no introduced findings against the 1,272-issue pre-change
baseline (572 existing errors). The final local APK and screenshots use `1.2.0-284` metadata. The release
pipeline independently rebuilds the pinned source commit with the same version.
[Interface and folding design](ui/NATIVE-REDESIGN.md) and
[local validation data](ui/native/validation.json) describe the evidence.

The existing release workflow gates publication on build, JVM tests, Lint,
signature/manifest checks, API 23/26/33/36 UI and upgrade checks, and offline
update recovery. This version adds the eight adaptive UI checks to each device
and an unfolded-window run on API 36. The completed CI results are recorded above.

Fixtures use synthetic library data and documents. Physical foldable devices,
authenticated library synchronization and licensed PDF reader behavior are not
certified by these fixtures.
