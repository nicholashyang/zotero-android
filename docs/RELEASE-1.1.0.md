# Zotero 1.1.0

Android build: `1.1.0-283`. Release tag: `dev-v1.1.0-283`.

## Changes

- Library rows display the downloaded main attachment's PDF first page, EPUB 2/3 cover or image thumbnail.
- Cover generation runs off the UI thread for visible rows, with bounded memory and disk caches. It does not download attachments or contact external cover services.
- Missing, unsupported and unreadable covers retain the item type icon in a consistently sized placeholder.
- Attachment downloads, file revisions and download removal refresh the displayed cover. Library-scoped deletion notifications also refresh the correct rows.

## Verification

Before release preparation, the development APK built successfully, all 73 JVM tests passed, and the API 35 cover/list/mobile subset passed 28 tests. All eight cover tests also passed against the final feature APK, and its fixture screenshot was inspected. New cover source files had no Lint findings; the repository retains its existing Lint baseline.

The release workflow builds the pinned source commit, checks the APK signature and update manifest, and gates publication on API 23, 26, 33 and 36 device jobs. These jobs check retention when upgrading the previous published APK, run the existing UI/update suite plus the new cover tests, and exercise offline download recovery. The cover screenshot fixture supports Android versions before API 26.

Tests use synthetic documents and an isolated application. Account-backed attachment synchronization and a live account library are outside this validation.
