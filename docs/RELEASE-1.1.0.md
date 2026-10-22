# Zotero 1.1.0

Android build: `1.1.0-283`. Release tag: `dev-v1.1.0-283`.

## Changes

- Library rows display the downloaded main attachment's PDF first page, EPUB 2/3 cover or image thumbnail.
- Cover generation runs off the UI thread for visible rows, with bounded memory and disk caches. It does not download attachments or contact external cover services.
- Missing, unsupported and unreadable covers retain the item type icon in a consistently sized placeholder.
- Attachment downloads, file revisions and download removal refresh the displayed cover. Library-scoped deletion notifications also refresh the correct rows.

## Published release

- [Zotero 1.1.0-283](https://github.com/nicholashyang/zotero-android/releases/tag/dev-v1.1.0-283) is public, marked Latest and not a prerelease.
- The [APK](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.1.0-283/Zotero-dev-debug.apk) and [update manifest](https://github.com/nicholashyang/zotero-android/releases/download/dev-v1.1.0-283/update-dev.json) are available publicly.
- The tag points to [c9916d4169c01d2e04d899690a9335757a3eafd7](https://github.com/nicholashyang/zotero-android/commit/c9916d4169c01d2e04d899690a9335757a3eafd7). The [release workflow](https://github.com/nicholashyang/zotero-android/actions/runs/39841605392) passed build, all four device jobs and publication.
- The actual CI APK was checked for package metadata, signature, size and SHA-256. The public release asset digests match that APK and its generated update manifest. APK size: 183,850,564 bytes; SHA-256: `87e9fae37f17891660b1bf1c4b5d886942805863c5d6d264ccaecc0ace501596`.

| Android API | UI / update / cover tests | Offline recovery tests | 282-to-283 data marker |
| --- | --- | --- | --- |
| 23 | 50 passed | 4 passed | Retained |
| 26 | 50 passed | 4 passed | Retained |
| 33 | 50 passed | 4 passed | Retained |
| 36 | 50 passed | 4 passed | Retained |

The CI JVM report records 73 tests with no failures or skips. The Lint report retains 1,278 existing project/environment findings and no findings in the new cover files. [Machine-readable acceptance results](validation/release-1.1.0.json) record the published artifact and test matrix.

## Verification

Before release preparation, the development APK built successfully, all 73 JVM tests passed, and the API 35 cover/list/mobile subset passed 28 tests. All eight cover tests also passed against the final feature APK, and its fixture screenshot was inspected. New cover source files had no Lint findings; the repository retains its existing Lint baseline.

The local `1.1.0-283` APK also built successfully and passed all 73 JVM tests. Its application ID and signing certificate match the published 282 APK. An API 35 fixture retained its data marker through a real 282-to-283 overwrite installation, then passed all 50 UI/update/cover tests and four offline download recovery tests. The previous APK's size and SHA-256 were checked against its current public update manifest before this run.

The release workflow builds the pinned source commit, checks the APK signature and update manifest, and gates publication on API 23, 26, 33 and 36 device jobs. These jobs check retention when upgrading the previous published APK, run the existing UI/update suite plus the new cover tests, and exercise offline download recovery. The cover screenshot fixture supports Android versions before API 26.

Tests use synthetic documents and an isolated application. Account-backed attachment synchronization and a live account library are outside this validation.
