# Manual Google Play release procedure

This checklist applies to one exact candidate revision on `main`. Its immutable `vX.Y.Z` tag
identifies the source used to build the signed AAB. The GitHub Release starts as a prerelease while
Google Play reviews it and becomes stable only after Play reports the app as published. Never put
keystores, passwords, tokens, or other credentials in the repository, command lines, logs, issues,
Pull Requests, or GitHub Release assets.

## Before releasing

1. Fetch `main` and verify the release Pull Request is merged.
2. Confirm `version.properties` contains the intended strict SemVer name and a version code greater
   than every code previously uploaded to Google Play.
3. Confirm repository validation and the required app checks passed for the selected commit.
4. Prepare English, Spanish, and Catalan Play release notes and confirm the listing and policy
   declarations match the app being submitted.

## Step 1: Freeze and tag the candidate

- Record the full commit SHA from `main` and confirm its `version.properties` identity.
- Create and push the immutable annotated tag `vX.Y.Z` at that commit. Do not move or reuse it.
- Confirm the GitHub tag validation workflow passes before building the signed bundle.

## Step 2: Generate the AAB from the tagged revision

Check out the tagged revision in Android Studio, then:

- Select **Build > Generate Signed Bundle/APK**.
- Select **Android App Bundle** and click **Next**.
- Select the NumPairs module and click **Next**.
- Select the dedicated upload keystore and alias, enter the passwords, choose the `release` variant,
  and generate the bundle.
- Keep the exact output path. Store the keystore outside the repository, with an encrypted backup
  independent from the development machine. Keep its alias and passwords in a password manager.
- Do not rebuild from a later `main` commit; the bundle must come from the tagged source.

## Step 3: Verify and record the signed AAB

- Verify the signature and upload certificate as described in
  [Manual Upload Signing](release-signing.md).
- Calculate the AAB's SHA-256 checksum and record it beside the full source commit SHA.
- Keep the signed AAB private; record its checksum, not the binary, in the public release record.

## Step 4: Publish a GitHub prerelease

- Create a GitHub Release for the candidate tag with the version name and code, source commit,
  AAB SHA-256, and English, Spanish, and Catalan release notes.
- Mark it as a prerelease and publish it before submitting the candidate to Production.
- Do not attach the signed AAB or any signing credentials.

## Step 5: Validate the artifact through Google Play

- Upload the exact signed AAB to Internal testing and install the Play-delivered build for the
  release smoke check.
- Promote that same artifact through Closed testing when required; never rebuild between tracks.
- For a new personal account, satisfy the continuous tester requirement and obtain Production
  access before submitting the candidate.

## Step 6: Submit to Production and wait for publication

- Select or promote the already uploaded version in **Test and release > Production**. Confirm the
  displayed version name and code match the tagged candidate; do not upload a replacement bundle.
- Complete the Play review and publication steps, resolving any blocking errors.
- Monitor Play Console submission activity. `Ready to publish` means review passed but still
  requires the developer to publish; wait for `Published` and verify the release is active.

## Step 7: Finalize the public release record

- After Play reports `Published`, edit the GitHub Release and clear its prerelease status. Keep
  the tag fixed to the same commit.
- Record the Play submission identity and active Production status in the release issue, then
  cross-check the app identity, commit, tag, checksum, and GitHub Release.

## Rejected submissions

- If the rejection concerns listing or policy metadata and the AAB is unchanged, correct the
  metadata and resubmit the same artifact. Keep the candidate tag and prerelease.
- If code changes require a replacement AAB, retain the rejected candidate and prepare a new
  SemVer name, immutable tag, and version code greater than every code already uploaded.
- Never delete or move a candidate tag to make a replacement appear to be the original build.
