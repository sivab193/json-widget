# Play Store launch checklist

## Code and build

- [x] Target and compile Android API 36.
- [x] Request promoted ongoing notifications only for active transit and delivery sessions.
- [x] Provide explicit Refresh and Stop actions and automatically end sample sessions.
- [x] Keep arbitrary JSON, events, and family locations out of promoted Live Updates.
- [x] Build a fresh-clone debug APK without a repository-local debug keystore.
- [x] Load release signing credentials only from environment variables.
- [x] Disable backup and device transfer for credentials and cached payloads.
- [ ] Create and securely back up the Play upload key.
- [ ] Produce and verify a signed release AAB.
- [ ] Run physical-device tests on Android 13, 15, and 16+, including a Samsung device.
- [ ] Record the foreground-service declaration video on a physical phone.

## Play Console

- [ ] Create the app with package `dev.sivab.appnotify` and enable Play App Signing.
- [ ] Host `PRIVACY_POLICY.md` as a public HTTPS webpage and add the actual developer/support contact.
- [ ] Complete Data Safety, ads, content rating, target audience, and app-access declarations.
- [ ] Declare `specialUse` foreground service for time-bounded, user-initiated transit/delivery tracking.
- [ ] Upload the signed AAB to Internal testing first.
- [ ] Add a 512×512 store icon, 1024×500 feature graphic, and phone screenshots.
- [ ] If the developer account is subject to it, complete the required closed test before production access.

## Claims to keep accurate

- Home-screen widgets are supported; lock-screen widget hosting is device-dependent and is not promised.
- Android 16 promoted Live Update eligibility is requested, not guaranteed.
- Samsung controls Now Bar placement.
- Galaxy Fit mirroring depends on Galaxy Wearable notification settings.
