# Manual Google Play release procedure

This checklist applies to the exact merged `main` revision selected for release. Never put
keystores, passwords, tokens, or other credentials in the repository, command lines, logs, issues,
Pull Requests, or GitHub Release assets.

## Before releasing

1. Open a release Pull Request from a branch based on the current `main`.
2. Update the repository version source of truth with the intended numeric SemVer `versionName`
   and a `versionCode` greater than every code previously uploaded for `org.cescfe.numpairs`.
3. Add release notes, complete review and CI, merge the Pull Request, and record its merge commit.

## Step 1: Validate the repository

From the merged revision, run:

```bash
./gradlew spotlessApply testDebugUnitTest spotlessCheck compileDebugAndroidTestKotlin
./gradlew lintDebug
./gradlew bundleRelease
```

The Gradle release bundle produced without IDE signing credentials is only a compilation check; do
not upload it. Stop if any command fails.

## Step 2: Generate the signed AAB from Android Studio

- Select **Build > Generate Signed Bundle/APK**.
- Select **Android App Bundle** and click **Next**.
- Select the NumPairs module and click **Next**.
- Select the dedicated upload keystore and alias, enter the passwords, choose the `release` variant,
  and generate the bundle.
- Keep the exact output path. Store the keystore outside the repository, with an encrypted backup
  independent from the development machine. Keep its alias and passwords in a password manager.

See the [Android signing documentation](https://developer.android.com/studio/publish/app-signing).

## Step 3: Verify the artifact

Use the same unchanged AAB for every track. Before uploading it, run:

```bash
jarsigner -verify -verbose -certs /path/to/numpairs-release.aab
keytool -printcert -jarfile /path/to/numpairs-release.aab
sha256sum /path/to/numpairs-release.aab
```

The first command must report `jar verified`. Confirm that the signer certificate matches the
registered upload key. Record the AAB filename, `versionName`, `versionCode`, merged commit, and
SHA-256 checksum in the release record.

## Step 4: Upload to Internal testing

- Open **Play Console > NumPairs > Test and release > Internal testing**.
- Select **Create new release**, upload the verified AAB, and write the release notes.
- Review and roll out the release to Internal testing.
- Install the Play-delivered build with an eligible tester and perform representative gameplay and
  upgrade checks.

See Google's [release preparation guide](https://support.google.com/googleplay/android-developer/answer/9859348)
for current console labels and review steps.

## Step 5: First publication — Closed testing and Production access

For a personal developer account created after 13 November 2023, Internal testing is optional but
initial publication requires a Closed test with at least 12 testers continuously opted in for 14
days. Testers who leave and rejoin do not satisfy the continuous period. Closed-test participants
must leave Internal testing first, or use a separate eligible account.

- Open **Test and release > Closed testing**, create or manage the required track, and promote the
  exact same AAB without rebuilding it.
- Add eligible testers, share the opt-in link, and keep at least 12 opted in continuously for the
  required period.
- From the Play Console Dashboard, select **Apply for production**, answer the current testing,
  app/game, and production-readiness questions, and wait for Google's decision.
- After Production access is granted, promote the same version code and unchanged AAB to
  **Production** and complete the release review.

Check the [current personal-account testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465)
before starting; Google may change eligibility or policy details.

## Step 6: Routine releases after Production access

For later updates, increment `versionCode`, select the appropriate SemVer `versionName`, merge a
release Pull Request, and repeat validation, signing, verification, and Internal testing. Promote
that same AAB from Internal to Production. Closed testing remains available for higher-risk changes
but is not a permanent gate unless Play Console requires it.

Every upload consumes its `versionCode`, including rejected, withdrawn, or pre-production uploads.
If a candidate fails before Production, fix it in a new Pull Request and upload a new, higher
`versionCode`; the previous code cannot be reused. After a release reaches Production and is tagged,
a correction also requires a new SemVer `versionName`.

## Step 7: Record Production only after Play confirmation

Do not create the production tag or GitHub Release until Play Console confirms the Production
promotion. Then, on the exact merged commit:

```bash
git tag -a vX.Y.Z <merge-commit> -m "Release vX.Y.Z"
git push origin vX.Y.Z
gh release create vX.Y.Z --target <merge-commit> --title "NumPairs vX.Y.Z" --notes-file release-notes.md
```

Use an immutable tag matching `versionName`. The GitHub Release must record the app version,
`versionCode`, source commit, release notes, and AAB checksum. Do not attach the signed AAB or
publish private signing material.

## Failure recovery and evidence

- Before Production, keep the candidate untagged, retain its checksum and Play track history, and
  upload a corrected AAB with a new `versionCode`.
- After Production, never move or recreate the production tag. Prepare a new release identity and
  a new `versionCode` for the correction.
- Preserve the keystore backup, tester opt-in evidence, Play review outcome, artifact checksum,
  merge commit, tag, and GitHub Release together in the access-controlled release record.
- Re-check [Google Play policy and release documentation](https://support.google.com/googleplay/android-developer/answer/9859348)
  immediately before each release because console behavior and policy requirements can change.
