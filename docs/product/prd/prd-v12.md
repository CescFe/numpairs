# PRD - NumPairs 📦 v12 App Versioning & Google Play Delivery

> Product and delivery contract for the `v12 - App Versioning & Google Play Delivery`
> milestone.
> Status: planned.
> The delivered v11 Simplified Play Modes product is the application baseline.

## Product Summary

NumPairs currently has an Android `versionCode` and `versionName`, product milestones named
`v0` through `v11`, and independently versioned persisted data. Those values serve different
purposes, but the repository does not yet define their relationship or provide a repeatable path
from one exact source revision to a Google Play release.

v12 establishes app-release identity as its own contract. It introduces a strict SemVer release
name, a monotonically increasing Google Play version code, one repository source of truth,
reproducible local upload signing, CI guardrails, a documented manual release path, and a later
transition to protected release automation.

The active Google Play candidate takes its release identity from the repository source of truth
at release-preparation time. Its release name reflects the intended SemVer outcome, and its
version code is greater than every code previously uploaded for the package. Product milestone
labels, persisted aggregate schemas, and Daily recipe versions remain independent and are not
derived from that release identity.

## Product Goal

Make every distributed NumPairs build identifiable, reproducible, secure, and traceable from its
repository revision through Google Play, while keeping the first release process understandable
and manually controlled before automating external publication.

## Version Vocabulary

### App Version Name

`versionName` is the player-visible release name. NumPairs uses strict numeric SemVer:

```text
MAJOR.MINOR.PATCH
```

- `MAJOR` changes only for a deliberately incompatible application generation.
- `MINOR` changes for a release whose primary outcome adds player-visible capability.
- `PATCH` changes for compatible fixes, quality improvements, or release corrections.

Testing-track state is represented by Google Play rather than prerelease suffixes. The repository
therefore stores no `alpha`, `beta`, or track suffix in `versionName`. GitHub marks a tagged
candidate release as a prerelease while Google Play reviews it; the release becomes stable when
Play reports it as published.

### App Version Code

`versionCode` is the positive integer used by Android and Google Play to order builds. Every AAB
uploaded to any Google Play track must use a code greater than every AAB previously uploaded for
the package, including a replacement for a rejected or withdrawn candidate.

A correction that changes only Play Console metadata may reuse the exact tagged artifact. Any
correction that requires a replacement AAB uses a new SemVer release name and a version code
greater than every code already uploaded. Candidate tags are immutable, so a version name already
tagged for a candidate cannot identify a different source revision or replacement artifact.

### Product Milestone

PRD labels such as `v11` and `v12` identify internal product and delivery milestones. They do not
set, imply, or increment Android `versionName` or `versionCode`.

### Persisted Schema And Recipe Version

Generated-session schema versions, Daily aggregate schema versions, and Daily recipe versions
protect stored-data and deterministic-content contracts. They evolve only when their own formats
or recipes change and never because an app release is published.

## Repository Version Contract

The repository owns one explicit source of truth for `versionName` and `versionCode`. The Android
application module consumes those values for every build variant and fails configuration when a
value is missing, malformed, or outside its supported range.

The source of truth defines the active release candidate without prescribing one fixed identity
for the milestone. At release-preparation time:

- `versionName` is selected according to the intended SemVer release outcome
- `versionCode` is selected above every code previously uploaded for `org.cescfe.numpairs`

The committed pair and exact source revision identify the candidate. Release evidence records
that identity and the resulting artifact as it progresses through Google Play.

Release changes are reviewed through a dedicated Pull Request. CI compares changed values with
the target branch, rejects a non-increasing version code or decreasing SemVer name, and checks
that any release tag `vX.Y.Z` exactly matches the committed `versionName` and identifies a
revision on `main`.

Candidate tags are immutable and are pushed once the release source is frozen, before building the
signed AAB. Each candidate GitHub Release records the exact app version, version code, source
commit, AAB checksum, and release notes as a prerelease while Google Play reviews it. After Play
reports the release as published, the GitHub Release is marked stable. The private upload key and
signed AAB are never published.

## Signing And Key Ownership

NumPairs targets Google Play only for v12 and uses Play App Signing:

- Google generates and protects the app signing key used for installed APKs.
- The developer generates and controls a separate upload key used to sign AAB submissions.
- The upload key and its passwords remain outside the repository and GitHub Release assets.
- The upload keystore has a protected backup independent from the development machine.

The manual release phase uses Android Studio's supported workflow to generate the dedicated
upload key and create a signed release bundle. The keystore and its credentials remain outside
the repository, while CI uses Gradle's default unsigned release-bundle path for compilation
validation. Non-interactive signing and secret injection are introduced only with protected
release automation after the manual workflow has been proven.

## CI Guardrails

Continuous integration protects repository-owned release invariants without receiving publication
credentials during the manual phase. It must:

- validate strict SemVer and a positive integer version code
- require a changed version code to increase relative to the Pull Request base
- prevent SemVer regression
- reject reuse of a SemVer release name once its immutable `vX.Y.Z` candidate tag exists
- verify that a `vX.Y.Z` candidate tag matches the source version and belongs to `main`
- compile the release AAB without an upload key
- retain the existing formatting, lint, unit-test, and instrumented-test compilation coverage

CI does not publish, sign, or promote a Google Play release during the manual phase.

## Google Play Product Configuration

The first Play Console application uses these fixed decisions:

- package: `org.cescfe.numpairs`
- product type: game
- pricing: free
- default listing language: English
- translated listings: Spanish and Catalan
- availability: every supported country or region
- app signing: Google-managed app signing key with a developer-controlled upload key

The store listing, content rating, target audience, privacy policy, Data safety answers, app
access, advertising declaration, and every other app-content declaration must describe the exact
submitted release. v12 does not introduce accounts, analytics, advertising, billing, or remote
data collection merely to support publication.

GitHub Sponsors may support development outside the app. v12 adds no in-app, store-listing, or
payment CTA for Sponsors and offers no sponsor-only digital content or gameplay benefit.

## Manual Release Lifecycle

The manual path remains the source of truth until it has completed successfully:

1. Merge a release Pull Request containing the intended version identity and release notes.
2. Freeze the exact merged `main` revision, create its immutable `vX.Y.Z` candidate tag, and let
   CI validate the tag.
3. Build and verify one signed AAB from that tagged revision; record its SHA-256 checksum.
4. Publish a GitHub prerelease for the tag with the version identity, source commit, checksum, and
   release notes. Do not attach the signed AAB.
5. Upload that same AAB to Google Play Internal testing, validate the Play-delivered build, and
   promote the same artifact through any required testing tracks.
6. For the new personal developer account, keep at least 12 testers opted in continuously for the
   required 14-day period and apply for Production access.
7. Submit the same artifact to Production and track the Play submission until its status is
   `Published`; `Ready to publish` still requires the developer to publish it.
8. Once Play reports `Published` and the release is active, mark the GitHub prerelease as stable
   and cross-check the tag, commit, artifact checksum, and Play identity.

Internal testers who will participate in Closed testing must leave Internal first or use a
separate eligible account. If Play rejects a submission because of listing or policy metadata,
correct that metadata and resubmit the same tagged artifact. If a rejection requires a new AAB,
keep the rejected candidate record and prepare a new SemVer release name, source tag, and higher
version code; never reuse an uploaded code or move an existing tag.

After initial Production access, later releases normally move through Internal testing and then
Production. Closed testing remains available when release risk justifies it but is not treated as
a permanent gate unless Google Play requires it.

## Controlled Automation

Automation begins only after the manual process and its credentials are proven. It preserves the
same version, signing, artifact, and approval contracts:

- one protected GitHub environment owns least-privilege Internal-upload credentials
- a separate protected Production environment requires explicit approval
- an explicit `main` revision is validated, signed, and uploaded to Internal
- Production promotion selects the already uploaded Play artifact and never rebuilds it
- the immutable candidate tag and GitHub prerelease identify the source before Play submission
- a GitHub prerelease becomes stable only after Play confirms the release is published
- failed or partially published runs remain recoverable without reusing a version code

Automation does not grant general repository workflows access to the upload key or Production
permissions.

## Delivery Stages

1. Document the v12 contract and align the repository entry point.
2. Centralize and validate the Android app version identity.
3. Document IDE-managed manual upload signing and secret-safe key custody.
4. Add version, tag, and release-bundle CI guardrails.
5. Document and prepare the manual Google Play release procedure.
6. Configure the Play application, integrity, listing, and policy declarations.
7. Deliver and validate one exact selected candidate through Internal and required Closed testing.
8. Obtain Production access and publish the first traced Production release.
9. Provision protected, least-privilege release automation.
10. Automate Internal upload and separately approved Production promotion.
11. Align release documentation and validate the complete milestone.

## Out Of Scope

- Deriving the app version from PRD or Git milestone names
- Changing generated-session, Daily aggregate, or Daily recipe versions
- Gameplay, puzzle generation, navigation, visual design, or localization behavior changes
- Distribution through stores other than Google Play
- Paid downloads, in-app purchases, subscriptions, advertisements, or other monetization
- An in-app or Play Store GitHub Sponsors action
- Attaching the signed production AAB to a public GitHub Release
- Unapproved, unattended, or schedule-triggered Production publication
- Closing the GitHub milestone automatically

## Success Criteria

- App release, product milestone, persisted schema, and Daily recipe versions are explicitly
  independent.
- The repository provides one valid source of truth for the active app-release candidate.
- Manual release builds are signed through a documented supported IDE workflow without committing
  or publishing credentials.
- CI rejects invalid, non-monotonic, or tag-inconsistent release identity.
- One exact signed AAB reaches Internal, required Closed testing, and Production without rebuild.
- The selected Production identity is traceable from Google Play to one source commit, the exact
  promoted artifact, an immutable tag, and a GitHub Release.
- Internal upload and Production promotion use separate least-privilege and approval boundaries.
- Existing v11 gameplay and all persisted-data contracts remain unchanged.
