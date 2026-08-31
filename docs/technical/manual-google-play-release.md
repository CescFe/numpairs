# Manual Google Play release procedure

This checklist applies to the exact merged `main` revision selected for release. Never put
keystores, passwords, tokens, or other credentials in the repository, command lines, logs, issues,
Pull Requests, or GitHub Release assets.

## Before releasing

1. Ensure `main` branch is fetched.
2. Validate that `version.properties` is increased.
3. Validate that `instrumented tests` run successfully.
4. Run the app to be sure it works.

## Step 1: Generate the AAB from Android Studio

From Android Studio pointing to the main branch:

- Select **Build > Generate Signed Bundle/APK**.
- Select **Android App Bundle** and click **Next**.
- Select the NumPairs module and click **Next**.
- Select the dedicated upload keystore and alias, enter the passwords, choose the `release` variant,
  and generate the bundle.
- Keep the exact output path. Store the keystore outside the repository, with an encrypted backup
  independent from the development machine. Keep its alias and passwords in a password manager.

## Step 2: Publish it from Google Play Console

- Open Google Play Console and select `Numpairs`.
- Open **Test and release > Closed testing**.
- Select **Create New Release** and upload the AAB generated in the previous step.
- Write the release notes in English.
