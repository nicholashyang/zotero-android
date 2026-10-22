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
and an unfolded-window run on API 36. Actual CI results are recorded after the
workflow completes.

Fixtures use synthetic library data and documents. Physical foldable devices,
authenticated library synchronization and licensed PDF reader behavior are not
certified by these fixtures.
