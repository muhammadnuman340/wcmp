# WCMP Phase 4 / Step 3 — Audio Evidence Capture & Active Recording UI

## Implementation Status
**Complete.** The implementation successfully replaces the placeholder audio strategy with a concrete, Android-compliant architecture using `MediaRecorder` for authentic foreground microphone capture.

## Architecture
This phase extends the exact pattern established in Phase 4 / Step 2 to ensure secure, isolated capture:
```
EvidenceDiagnosticsViewModel / AppLockController
                    ↓
          EvidenceCaptureCoordinator
                    ↓
             EvidencePolicy
                    ↓
        AndroidAudioCaptureStrategy
                    ↓
            AudioCaptureSession (Suspends & Bridging)
                    ↓
          AudioCaptureActivity (Foreground Context)
                    ↓
          AudioCaptureScreen (Compose UI)
                    ↓
          MediaRecorder (Android API)
                    ↓
        App-private audio file (.m4a)
                    ↓
             EvidenceRecord
                    ↓
            EvidenceStorage
                    ↓
             VaultEvidenceDao
                    ↓
                Vault UI
```

## Android Recording API
- **API**: `android.media.MediaRecorder`.
- **Audio Format**: `MPEG_4` container with `AAC` encoding.
- **Maximum Duration**: 60 seconds (automatically stops and saves upon reaching the limit).

## Files Created
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/evidence/AudioCaptureSession.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/evidence/capture/audio/AudioCaptureUiState.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/evidence/capture/audio/AudioCaptureViewModel.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/evidence/capture/audio/AudioCaptureScreen.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/evidence/capture/audio/AudioCaptureActivity.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/core/evidence/AudioCaptureSessionTest.kt`

## Files Modified
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/evidence/AndroidAudioCaptureStrategy.kt`: Replaced placeholder simulation with a real `AudioCaptureSession` launch and await.
- `c:/numan/Numanx-projacts/wcmp/app/src/main/AndroidManifest.xml`: Registered `AudioCaptureActivity`.

## Permission Behavior
The application requests `android.permission.RECORD_AUDIO` exclusively inside the foreground capture flow (`AudioCaptureActivity`). If denied, the UI distinctly transitions to a blocked "Permission Required" state. No hidden background mic activations attempt to bypass user consent.

## Active Recording UI & Lifecycle
The `AudioCaptureScreen` adheres strictly to the WCMP dark-navy aesthetic, rendering clear states:
1. **Idle/Ready**: Awaits user explicit action (`START RECORDING`).
2. **Recording**: Displays an animated red pulse indicator and live elapsed timer.
3. **Completion**: Provides feedback that the evidence was saved with the generated `recordId`.
4. **Cleanup**: Users have explicit `CANCEL` capabilities which halt `MediaRecorder`, safely release the codec, delete the unfinished `m4a` temporary chunk, and resume the suspended domain coroutine gracefully.

## Storage Behavior
Files are placed exclusively inside isolated private app storage (`context.filesDir/evidence/audio_<timestamp>_<uuid>.m4a`). Public Android MediaStore or external paths are entirely avoided, guaranteeing evidence cannot be accessed by other malicious apps on the device.

## Trigger Classification & Vault Integration
The Vault seamlessly integrates the generated `EvidenceRecord`. Diagnostics tests trigger an explicit `MANUAL_TEST` and map to `VaultEvidenceType.AUDIO`, rendered elegantly without attempting to play back the stream (as per strict explicit instructions to not invent custom media playback UI at this stage).

## Security Boundaries & Platform Limitations Enforced
- **No Covert Recording**: All audio is explicitly recorded only while the UI is active and explicitly requested.
- **No Background Microphone Bypass**: The Android 11+ background microphone limits are fully respected.
- **Application-level evidence encryption is NOT IMPLEMENTED in this step**, per instructions not to enforce it simultaneously.

## Tests and Build Result
- **Unit Tests**: `AudioCaptureSessionTest` validates the coroutine continuation boundaries (suspend cancellation, completion with `EvidenceRecord`, unbinding, memory leak prevention).
- **Build**: `./gradlew test assembleDebug` executes efficiently, resolving Kotlin 2.1 / KSP compilation smoothly.

## Recommended Next Step
Proceed to Phase 4 / Step 4 — Location Tracking & Evidence Integration.
