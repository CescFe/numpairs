# Android 17 / API 37 Migration Notes

Last reviewed: 2026-06-21

## SDK Configuration

- `compileSdk`: Android 17 / API 37
- `targetSdk`: Android 17 / API 37
- `minSdk`: unchanged at API 26

## Behavior Changes Review

Reviewed the Android 17 behavior changes for all apps and for apps targeting Android 17 or higher.

The current NumPairs app is a single-activity Jetpack Compose game with no declared runtime permissions and no background services, media playback, Bluetooth, contacts, SMS, custom native dynamic code loading, custom content providers, or explicit URI sharing flows.

No code or manifest changes were required for the Android 17 target SDK migration.

### Relevant Changes Confirmed As No-op

- App memory limits: no app-specific change required. NumPairs should remain covered by normal app memory smoke testing.
- SMS OTP protection: no SMS permissions or OTP extraction flows.
- Cleartext traffic and network security: no cleartext traffic configuration or direct network flows.
- Implicit URI grants: no `ACTION_SEND`, `ACTION_SEND_MULTIPLE`, or `ACTION_IMAGE_CAPTURE` flows.
- Per-app keystore limits: no Android Keystore usage.
- Cross-profile loopback traffic: no loopback networking.
- IME visibility after rotation: no required always-visible keyboard flow.
- Pointer capture and touchpad behavior: no pointer capture usage.
- MessageQueue behavior changes: no direct `MessageQueue` usage.
- Static final field mutation restrictions: no reflection-based mutation of `static final` fields.
- Local network protection: no local network socket flows or local network permissions.
- Standard SMS OTP protection for target API 37: no SMS permissions.
- Background activity launch restrictions: no background launch or `IntentSender` flows.
- Certificate transparency enabled by default: no custom networking stack or certificate pinning.
- Safer native dynamic code loading: no `System.load()` or native dynamic code loading.
- Contacts Provider 2 privacy and SQL restrictions: no contacts provider queries.
- Background audio hardening: no media playback or audio focus usage.
- Large screen orientation and resizability behavior: no orientation, aspect ratio, or resizability restrictions declared.
- Bluetooth RFCOMM socket `read()` behavior: no Bluetooth socket usage.

## Validation

Required validation for this migration:

- `spotlessCheck`
- debug Kotlin compilation
- android-test Kotlin compilation
- debug unit tests
- smoke test on an API 37 emulator or device covering launch, menu navigation, Four Pairs gameplay, TopAppBar Help/Hint actions, dialogs, and persisted preference state
