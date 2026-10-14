# WCMP (Who Checked My Phone) - Release Readiness

## 1. Application Identity
- **Application ID**: `com.numan.wcmp`
- **Version**: `1.0.0-rc1` (Version Code: 1)
- **Min SDK**: 29
- **Target SDK**: 35

## 2. Build Environment
- **Compile SDK**: 35
- **Kotlin**: 1.9.24
- **AGP**: 8.5.2
- **Java**: 11
- **Shrinker**: R8 (Enabled for release)

## 3. Architecture Summary
- Clean Architecture (Core, Data, Domain, Feature layers).
- Jetpack Compose M3 UI.
- Local Storage: Room Database v5 & DataStore Preferences.
- Coroutines / StateFlow for reactive UI and background IO processing.

## 4. Security Summary
- **Storage**: All evidence is AES-GCM encrypted and stored in `filesDir/evidence`.
- **Key Management**: Keys are generated and stored inside the Android KeyStore.
- **Biometrics**: AppLock enforces BiometricPrompt / Device Credential fallback to access sensitive Vault features.
- **Tamper Detection**: Health Evaluator detects `INTEGRITY_ERROR`, `MISSING_FILE`, and `KEY_UNAVAILABLE`.

## 5. Privacy Summary
- **Network**: Completely offline. No HTTP networking dependencies exist.
- **Analytics**: Zero telemetry, tracking, or crash reporting SDKs.
- **Screenshots**: Blocked via `FLAG_SECURE` on sensitive activities and the Compose Vault screen.
- **Backups**: Explicitly disabled (`android:allowBackup="false"`).
- **Logging**: Production source has been scrubbed of all `Log.d` and stack trace leakage.

## 6. Automated Test Results
- **JVM Unit Tests**: 54/54 Passed. Code coverage spans the Encryption pipeline, Health Evaluator, Room Migrations, and Security Session models.

## 7. Build Results
- **Debug**: `assembleDebug` builds successfully.
- **Release**: `assembleRelease` builds successfully (minified).

## 8. Release Artifact Information
- The release signing configuration is bound to environment variables. Without them, `./gradlew assembleRelease` safely produces an unsigned `app-release-unsigned.apk`.

## 9. Hardware Verification Status
- **Physical Device**: NOT VERIFIED
- **Emulator**: NOT VERIFIED

## 10. Known Limitations
- **Room Migrations**: Due to missing historical `.json` schema exports for versions 1, 2, 3, and 4, automated SQLite SQLite migration assertions using `MigrationTestHelper` cannot be fully executed.
- **Hardware Abstractions**: CameraX, MediaRecorder, FusedLocationProvider, and AccessibilityService transition reliability require physical device testing.

## 11. Manual QA Checklist
Execute the following on a physical Android device prior to production rollout:

### Installation & Home
- [ ] Install release APK.
- [ ] Launch application without crash.
- [ ] Verify Dashboard protection states and event counts.

### Protected Apps
- [ ] Verify discovery of installed packages.
- [ ] Protect an app and confirm persistence on restart.

### Accessibility Monitoring
- [ ] Enable Accessibility Service.
- [ ] Launch a protected app and verify a Security Event is recorded.

### App Lock & Biometrics
- [ ] Trigger App Lock on Vault access.
- [ ] Authenticate successfully.
- [ ] Cancel authentication and verify access is denied.

### Evidence Capture
- [ ] **Camera**: Grant permission, capture, verify encrypted Room presence.
- [ ] **Audio**: Grant permission, record, verify encrypted Room presence.
- [ ] **Location**: Grant permission, fetch coordinate, verify encrypted Room presence.

### Vault & Diagnostics
- [ ] Verify decrypted evidence displays correctly in the Vault.
- [ ] Verify Diagnostics tab accurately reflects current system permissions and health.
- [ ] Verify screenshot capture is blocked on the Vault.

## 12. Final Release Criteria
- **REQUIRES MANUAL QA**. Once the manual QA checklist above passes on a physical device, WCMP is ready for Google Play submission.
