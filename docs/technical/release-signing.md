# Manual Upload Signing

NumPairs uses Android Studio's supported signing workflow during the manual Google Play release
phase. The upload key, its passwords, and every private signing value must remain outside the
repository and must not be attached to a GitHub Release.

Non-interactive signing does not belong to the manual phase. It will be introduced with the
protected release automation after the manual workflow and its credentials have been proven.

## Create And Protect The Upload Key

Create a dedicated NumPairs upload key by following Android Studio's
[official app-signing workflow](https://developer.android.com/studio/publish/app-signing):

1. Select **Build > Generate Signed Bundle/APK**.
2. Select **Android App Bundle** and continue.
3. Select **Create new** below the key store path.
4. Choose a keystore path outside the repository and enter a dedicated alias and strong
   passwords.
5. Set the certificate validity to at least 25 years and complete the certificate identity.
6. Confirm the dialog to create the keystore.

Keep the keystore outside the repository. Store an encrypted backup independently from the
development machine, and keep a recoverable copy of its alias and passwords in an appropriate
password manager. Verify that the backup can be restored before relying on it for a release.

Common keystore formats are ignored by the repository as an additional safeguard. Never add the
keystore, its passwords, or other signing secrets to repository files, command lines, logs,
issues, Pull Requests, or GitHub Release assets.

The local key becomes the NumPairs upload key when its public certificate is registered with
Google Play. Google Play App Signing separately generates and protects the app signing key used
for APKs delivered to players.

## Build The Signed Release Bundle

Use the same supported Android Studio workflow for every manual release:

1. Select **Build > Generate Signed Bundle/APK**.
2. Select **Android App Bundle** and continue.
3. Select the dedicated NumPairs keystore and alias, then enter their passwords.
4. Select the `release` build variant and create the bundle.
5. Retain the exact output path reported by Android Studio for signature verification and Play
   upload.

Android Studio deliberately receives the signing values for this one manual operation. The
repository does not persist a release `signingConfig` or a private signing-property contract.

## Verify The Signed Bundle

Verify the AAB before uploading it:

```bash
jarsigner -verify -verbose -certs /path/to/signed-app-release.aab
```

The command must finish with `jar verified`. Inspect the bundle signer and the upload key
separately:

```bash
keytool -printcert -jarfile /path/to/signed-app-release.aab
keytool -list -v -keystore /secure/path/numpairs-upload.jks -alias numpairs-upload
```

Confirm that their SHA-256 certificate fingerprints match. Treat any unsigned result,
verification error, or unexpected signer as a release blocker.

## Unsigned CI Validation

Without an IDE-injected signing key, Gradle retains its credential-free release-compilation path:

```bash
./gradlew bundleRelease
```

The resulting AAB is unsigned and exists only to validate release compilation in CI. It must not
be uploaded to Google Play. Protected automation will later add its own non-interactive signing
and secret-injection contract without changing this manual workflow retroactively.
