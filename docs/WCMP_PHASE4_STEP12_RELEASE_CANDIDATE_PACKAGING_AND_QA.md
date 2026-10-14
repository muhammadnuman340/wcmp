# WCMP Phase 4 Step 12: Release Candidate Packaging & QA

## 1. Executive Summary
This document concludes Phase 4 Step 12 for WCMP ("Who Checked My Phone"). The `app/build.gradle.kts` configuration was hardened with a release signing placeholder, ProGuard minification defaults (`isMinifyEnabled = true`), and the application version was incremented to `1.0.0-rc1`. A comprehensive QA Matrix has been established, and all critical pipeline steps (`assembleRelease`, `lint`, and JVM testing) passed safely.

## 2. Project Audit
The WCMP architecture consists of distinct domain layers (`core`, `data`, `domain`, `feature`). Key components:
- `EvidenceKeyManager`: AES-GCM KeyStore wrapper. Repaired in Step 11 to enforce `KEY_UNAVAILABLE` over invalid fallback creation.
- `EvidenceCrypto`: Executes cryptographic operations over `Dispatchers.IO`.
- `AppLockActivity`: Biometric fallback implementation (now suppresses intentional back-button trap).
- Room Database (v5) implements distinct DAOs for `VaultEvidenceEntity`, `SecurityEventEntity`, and `ProtectedAppEntity`.

## 3. Build Configuration
- **Compile SDK**: 35
- **Target SDK**: 35
- **Min SDK**: 29
- **Version Name**: 1.0.0-rc1
- **Version Code**: 1

## 4. Release Configuration
- `isMinifyEnabled = true` is enabled in `release`.
- Custom `proguard-rules.pro` was added to protect Room entities and domain enums from being stripped.
- Release signing (`signingConfigs { create("release") }`) relies entirely on secure environment variables (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).

## 5. Security Audit
- Production source files were swept for `Log.`, `println`, and `printStackTrace`. None were found.
- The `EvidenceKeyManager` correctly utilizes `getExistingKey()` for decryption and health assertions.
- `EvidenceCrypto` safely destroys the plaintext evidence file even if AES encryption throws an exception.

## 6. Privacy Audit
- Zero analytics, Crashlytics, or remote tracking SDKs.
- `android:allowBackup="false"` is active.
- `FLAG_SECURE` is active for `CameraCaptureActivity`, `AudioCaptureActivity`, `LocationCaptureActivity`, `AppLockActivity`, and `VaultScreen`.

## 7. Manifest Audit
- Essential permissions: `CAMERA`, `RECORD_AUDIO`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `POST_NOTIFICATIONS`, `QUERY_ALL_PACKAGES`.
- Required explicit `<uses-feature android:name="android.hardware.camera" android:required="false" />` added to support lint constraints.
- `QUERY_ALL_PACKAGES` is strictly necessary to populate the "Protected Apps" discovery list.
- Only the `MainActivity` and `WcmpAccessibilityService` are explicitly exported.

## 8. R8/ProGuard Audit
- `proguard-rules.pro` protects `com.numan.wcmp.data.local.entity.**`, `com.numan.wcmp.domain.**`, and the `WcmpApplication` class. Minification operates safely.

## 9. Automated Tests
- `./gradlew test` (JVM): PASSED (54/54)
- `./gradlew testDebugUnitTest` (JVM): PASSED (54/54)

## 10. Debug Build
- `./gradlew assembleDebug`: PASSED.

## 11. Release Build
- `./gradlew assembleRelease`: PASSED.
- The release signing executes only if environment variables are detected; otherwise, it emits an unsigned release APK.

## 12. Artifact Verification
- Generated: `app-debug.apk` (Signed with local debug keystore)
- Generated: `app-release-unsigned.apk` (R8 Minified)

## 13. Evidence Storage Audit
- All files correctly persist to `context.filesDir/evidence`.
- No media is leaked to `MediaStore` or external SD storage.

## 14. KeyStore Verification
- Validated via Test Doubles: A missing KeyStore alias correctly throws `KEY_UNAVAILABLE`. A tampered ciphertext file throws `INTEGRITY_ERROR`.

## 15. Room Migration Status
- Migrations 1 -> 5 are mathematically correct and do not use destructive fallback.
- **KNOWN LIMITATION**: Historical `schema.json` artifacts for versions 1, 2, 3, and 4 are unavailable, preventing the execution of automated `MigrationTestHelper` assertions.

## 16. Manual QA Matrix
_See docs/WCMP_RELEASE_READINESS.md for the full manual execution list._

## 17. Emulator Verification
- **NOT VERIFIED**: Unavailable in CI environment.

## 18. Physical Device Verification
- **NOT VERIFIED**: Unavailable in CI environment.

## 19. Known Limitations
- The absence of physical hardware testing prevents empirical validation of CameraX framerates, MediaRecorder audio quality, GPS lock reliability, and real-world Accessibility Service bounds.
- Missing historical Room schema JSONs.

## 20. Remaining Release Requirements
- Physical Android Device Manual QA execution against the RC1 Build.
- CI/CD keystore injection to sign the production AAB.

## 21. Final Release Status
**REQUIRES MANUAL QA**
