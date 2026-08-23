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

The first Google Play candidate is `1.0.0 (3)`. Product milestone labels, persisted aggregate
schemas, and Daily recipe versions remain independent and are not derived from that release
identity.

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
therefore stores no `alpha`, `beta`, or track suffix in `versionName`.

### App Version Code

`versionCode` is the positive integer used by Android and Google Play to order builds. Every AAB
uploaded to any Google Play track must use a code greater than every AAB previously uploaded for
the package, including a replacement for a rejected or withdrawn candidate.

A replacement may retain its `versionName` while the intended release has not reached Production
and has no production tag. Once a release is active and tagged, every correction uses a new
SemVer release name and a new version code.

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

The source of truth starts at:

- `versionName`: `1.0.0`
- `versionCode`: `3`

Release changes are reviewed through a dedicated Pull Request. CI compares changed values with
the target branch, rejects a non-increasing version code or decreasing SemVer name, and checks
that any production tag `vX.Y.Z` exactly matches the committed `versionName`.

Production tags are immutable. Each production GitHub Release records the exact app version,
version code, source commit, and release notes without publishing the private upload key or
attaching the signed AAB by default.

## Signing And Key Ownership

NumPairs targets Google Play only for v12 and uses Play App Signing:

- Google generates and protects the app signing key used for installed APKs.
- The developer generates and controls a separate upload key used to sign AAB submissions.
- The upload key and its passwords remain outside the repository and GitHub Release assets.
- The upload keystore has a protected backup independent from the development machine.

Local Gradle properties provide the upload-keystore path, store password, key alias, and key
password. Supplying all four values produces a signed release bundle. Supplying none permits CI
to compile an unsigned release bundle. Supplying only part of the configuration fails clearly
instead of silently producing an unexpected artifact.

## CI Guardrails

Continuous integration protects repository-owned release invariants without receiving publication
credentials during the manual phase. It must:

- validate strict SemVer and a positive integer version code
- require a changed version code to increase relative to the Pull Request base
- prevent SemVer regression
- allow the same release name only for an untagged pre-production replacement
- verify that a `vX.Y.Z` tag matches the source version
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
2. Run repository validation and create one signed AAB from the exact merged `main` revision.
3. Verify the AAB signature and upload it to Google Play Internal testing.
4. Validate installation and representative app behavior through the Play-delivered build.
5. Promote the same AAB to Closed testing without rebuilding it.
6. For the new personal developer account, keep at least 12 testers opted in continuously for the
   required 14-day period and apply for Production access.
7. Promote the same version code to Production after the testing and policy gates succeed.
8. After Google Play confirms Production, create the immutable `vX.Y.Z` tag and GitHub Release on
   the exact source commit.

Internal testers who will participate in Closed testing must leave Internal first or use a
separate eligible account. A defect found before Production is corrected through another atomic
Pull Request and a higher version code; the previously uploaded code is never reused.

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
- the production tag and GitHub Release are created only after Play confirms the promotion
- failed or partially published runs remain recoverable without reusing a version code

Automation does not grant general repository workflows access to the upload key or Production
permissions.

## Delivery Stages

1. Document the v12 contract and align the repository entry point.
2. Centralize and validate the Android app version identity.
3. Make local upload signing reproducible and secret-safe.
4. Add version, tag, and release-bundle CI guardrails.
5. Document and prepare the manual Google Play release procedure.
6. Configure the Play application, integrity, listing, and policy declarations.
7. Deliver and validate `1.0.0 (3)` through Internal and required Closed testing.
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
- The repository provides one valid app-version source of truth beginning with `1.0.0 (3)`.
- Local release builds are reproducibly signed without committing or publishing credentials.
- CI rejects invalid, non-monotonic, or tag-inconsistent release identity.
- One exact signed AAB reaches Internal, required Closed testing, and Production without rebuild.
- `1.0.0 (3)` is traceable from Google Play to one source commit, immutable tag, and GitHub Release.
- Internal upload and Production promotion use separate least-privilege and approval boundaries.
- Existing v11 gameplay and all persisted-data contracts remain unchanged.
