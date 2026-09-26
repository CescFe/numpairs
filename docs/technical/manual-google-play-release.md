# Manual Google Play release procedure

This checklist applies to one exact release revision on `main`. Its immutable `vX.Y.Z` tag
identifies the source used to build the signed AAB. Create the tag with the **Create release tag**
workflow before building or submitting the AAB. Create a regular GitHub Release only after Google
Play reports the app as published. Never put keystores, passwords, tokens, or other credentials in
the repository, command lines, logs, issues, Pull Requests, or GitHub Release assets.

## Before releasing

1. Fetch `main` and verify the release Pull Request is merged.
2. Confirm `version.properties` contains the intended strict SemVer name and a version code greater
   than every code previously uploaded to Google Play.
3. Confirm repository validation and the required app checks passed for the selected commit.
4. Prepare English, Spanish, and Catalan Play release notes and confirm the listing and policy
   declarations match the app being submitted.

## Step 1: Freeze and tag the release revision

- Record the full commit SHA from `main` and confirm its `version.properties` identity.
- In GitHub Actions, run **Create release tag** on `main` and enter that full commit SHA.
- Confirm the workflow validates the release identity and creates the annotated `vX.Y.Z` tag.
- The tag is immutable. If the same release tag already points to this commit, the workflow treats
  the run as complete; it fails if that tag points elsewhere.

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

## Step 4: Validate the artifact through Google Play

- Upload the exact signed AAB to Internal testing and install the Play-delivered build for the
  release smoke check.
- Promote that same artifact through Closed testing when required; never rebuild between tracks.
- For a new personal account, satisfy the continuous tester requirement and obtain Production
  access before submitting the candidate.

## Step 5: Submit to Production and wait for publication

- Select or promote the already uploaded version in **Test and release > Production**. Confirm the
  displayed version name and code match the tagged candidate; do not upload a replacement bundle.
- Complete the Play review and publication steps, resolving any blocking errors.
- Monitor Play Console submission activity. `Ready to publish` means review passed but still
  requires the developer to publish; wait for `Published` and verify the release is active.

## Step 6: Create the public release record

- After Play reports `Published`, create a regular GitHub Release on the existing tag with the
  version name and code, source commit, AAB SHA-256, and English, Spanish, and Catalan release
  notes. Do not attach the signed AAB or any signing credentials.
- Record the Play submission identity and active Production status in the release issue, then
  cross-check the app identity, commit, tag, checksum, and GitHub Release.

## Rejected submissions

- If the rejection concerns listing or policy metadata and the AAB is unchanged, correct the
  metadata and resubmit the same artifact and tag.
- If code changes require a replacement AAB, retain the rejected candidate and prepare a new
  commit with an incremented SemVer name and version code greater than every code already uploaded.
  Run **Create release tag** for that commit, then build and submit its AAB.
- Never delete or move a tag to make a replacement appear to be the original build.
