# WCMP — Phase 3 / Step 4

## Camera & Audio Evidence Engine Foundation

### 1. Implementation Status
Completed Phase 3 / Step 4 successfully. A complete, platform-aware Evidence Engine Foundation was implemented. The architecture provides a clean domain boundary that translates downstream security events (`AUTHENTICATION_FAILED`) into evidence capture requests, strictly obeying user-configured policies and Android platform limitations.

### 2. Architecture
```text
SecurityEngine
        ↓
AppLockController (onAuthenticationCompleted -> FAILED/CANCELLED)
        ↓
EvidenceCaptureRequest
        ↓
EvidencePolicy (Validates configuration & trigger)
        ↓
EvidenceCaptureCoordinator
        ↓
EvidenceCaptureStrategy (AndroidCameraCaptureStrategy / AndroidAudioCaptureStrategy)
        ↓
EvidenceRecord
        ↓
EvidenceStorage (DefaultEvidenceStorage)
        ↓
VaultEvidenceDao / SecurityRepository
        ↓
Secure Vault UI
```

### 3. Files Created
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidenceTrigger.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidenceCaptureRequest.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidenceCaptureResult.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidencePolicy.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidenceCaptureStrategy.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidenceStorage.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/evidence/EvidenceCaptureCoordinator.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/repository/DefaultEvidenceStorage.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/evidence/AndroidCameraCaptureStrategy.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/evidence/AndroidAudioCaptureStrategy.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/domain/evidence/EvidencePolicyTest.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/domain/evidence/EvidenceCaptureCoordinatorTest.kt`

### 4. Files Modified
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/local/preferences/WcmpPreferences.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/repository/SecurityRepository.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/WcmpRepositoryProvider.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/protection/AppLockController.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/settings/components/EvidenceSettingsCard.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/settings/components/DiagnosticsCard.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/domain/protection/AppLockControllerTest.kt`

### 5. Camera Implementation
The `AndroidCameraCaptureStrategy` checks for `Manifest.permission.CAMERA`. However, it explicitly documents the Android background capture constraints natively. Currently, it yields a successful domain placeholder flow (`EvidenceRecord`) verifying full storage and coordinator capabilities natively without deceptively triggering background camera sensors illegally.

### 6. Audio Implementation
The `AndroidAudioCaptureStrategy` checks for `Manifest.permission.RECORD_AUDIO`. Like camera capture, it explicitly honors Android 11+ background foreground-service requirements. It yields a successful domain placeholder flow confirming architecture readiness.

### 7. Permission Handling
Camera and Microphone permissions are accurately mapped in the `DiagnosticsCard` using native `ContextCompat.checkSelfPermission`.
- When granted: Displayed as `"LIMITED (BG RESTRICTED)"` indicating Android OS lifecycle limits.
- When denied: Displayed as `"REQUIRES ACCESS"`.
No rogue permissions were requested upon startup.

### 8. Evidence Policy
Triggers configured in `EvidencePolicy` enforce that **only** `AUTHENTICATION_FAILED` (and developer `MANUAL_TEST` boundaries) ever result in evidence capture, and **only** if the user explicitly enabled `cameraEvidenceEnabled` or `audioEvidenceEnabled` in `WcmpPreferences`.

### 9. Storage
`DefaultEvidenceStorage` safely writes successful `EvidenceRecord` metadata straight into the existing `VaultEvidenceDao` architecture (`VaultEvidenceEntity`), persisting identically alongside standard security events. Physical storage is mocked safely into app-private `/data/user/0/com.numan.wcmp/files/evidence/` referencing native internal isolation boundaries. 
*Note: Application-level file encryption (e.g., EncryptedSharedPreferences/EncryptedFile) is technically reserved for later, so files reside cleanly in private app storage exclusively.*

### 10. Security Boundaries
The architecture rigorously respects the following privacy bounds:
- **Zero Covert Camera Activation**: Does not silently hijack the hardware camera.
- **Zero Covert Microphone Activation**: Does not secretly monitor room audio.
- **Zero Background Looping**: Avoids spinning up constant background recorders illegally.
- **Zero Third-Party UI Manipulation**: Does not record inside the protected app.
- **Zero Biometric Data Access**: Captures external environment only, ignoring system biometrics.
- **Zero Cloud Upload**: Everything is routed natively to Room/App-Private directories exclusively.
- **Zero SYSTEM_ALERT_WINDOW Hacks**: Refrains from breaking Android's security design deliberately.

### 11. Tests
- `EvidencePolicyTest`: Verified trigger boundaries and configuration flags.
- `EvidenceCaptureCoordinatorTest`: Verified strategy capabilities, error bubbling, and storage integrations safely.
- `AppLockControllerTest`: Updated gracefully to ensure legacy access-control tests maintain passing metrics while dispatching downstream evidence requests properly.

### 12. Build Results
- **assembleDebug**: `BUILD SUCCESSFUL`
- **test**: `BUILD SUCCESSFUL`

### 13. Manual Verification
Emulator testing verified `DiagnosticsCard` accurately reports `REQUIRES ACCESS` by default natively. Triggering Biometric Cancellation smoothly bubbles through `AppLockController` → `EvidenceCaptureCoordinator` correctly, halting accurately if capability is rejected.

### 14. Platform Limitations
Modern Android (11+) aggressively blocks background camera/audio recording unless driven by a visible UI (`Activity`) or a user-launched `ForegroundService` explicitly sporting the correct `camera`/`microphone` service-type. Consequently, background monitoring engines cannot silently trigger invisible captures seamlessly. 

### 15. Next Recommended Step
Proceed to **Phase 4 / Step 1 — Manual Tests & Diagnostics UI**. This step would connect the existing simulated capabilities (Camera/Audio testing) to dedicated developer buttons inside the Diagnostics UI to prove the Evidence storage Pipeline operates manually before wiring real visual/audio hardware APIs (e.g. CameraX).
