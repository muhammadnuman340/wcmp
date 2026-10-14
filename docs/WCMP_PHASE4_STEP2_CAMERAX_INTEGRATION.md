# WCMP Phase 4 / Step 2 — CameraX Integration & Active Capture UI

## Overview
This step implements the CameraX visual evidence capture pipeline for WCMP, replacing the previous placeholder simulation strategy with a concrete, Android-compliant architecture.

## Architectural Changes
We maintained the strict domain boundaries to ensure that camera capture remains highly controlled, explicit, and integrated cleanly with the pre-existing Evidence Engine logic. 

**Execution Path:**
1. `EvidenceDiagnosticsViewModel` (Manual Test) or `AppLockController` (Security Event) triggers `EvidenceCaptureCoordinator.requestCapture()`.
2. The coordinator delegates to the registered `AndroidCameraCaptureStrategy`.
3. Because Android 11+ prohibits background camera capture without foreground UI or service boundaries, `AndroidCameraCaptureStrategy` prepares a **`CameraCaptureSession`** and launches an explicit intent to **`CameraCaptureActivity`**.
4. The strategy suspends via a cancellable coroutine.
5. The `CameraCaptureActivity` (Compose UI) displays the CameraX preview and requests the `CAMERA` permission if it hasn't been granted.
6. The user explicitly captures the image. The image is saved to app-internal private storage (`files/evidence/camera_...jpg`).
7. The Activity completes the `CameraCaptureSession` with the generated `EvidenceRecord` and calls `finish()`.
8. The strategy resumes, returning the record up the chain to the coordinator.
9. `EvidenceCaptureCoordinator` saves the evidence to `EvidenceStorage` (the Secure Vault database), using the correct trigger category (`MANUAL_TEST` vs `AUTHENTICATION_FAILED`).

## Components Introduced
- **`CameraCaptureActivity`**: The foreground Activity required by modern Android to host the active camera preview and capture lifecycle.
- **`CameraCaptureScreen`**: A Jetpack Compose screen implementing the `PreviewView`, permission lifecycle, capture actions, and loading states, adhering to the WCMP dark cybersecurity theme.
- **`CameraCaptureViewModel`**: Manages the UI state (`CameraCaptureUiState`) and coordinates the CameraX `LifecycleCameraController.takePicture()` call.
- **`CameraCaptureSession`**: A singleton state holder bridging the domain strategy `suspend` function with the Android Activity lifecycle. It securely passes the capture results back to the caller when the activity finishes.

## Security & Storage Limitations Documented
- Images are strictly saved to `context.filesDir/evidence/` avoiding public external storage (Scoped Storage limitations avoided and privacy preserved).
- The `EvidenceRecord` acknowledges `isEncrypted = false` as encryption layers will be stacked in a subsequent step.
- Android platform policies prohibiting background capture are fully respected by enforcing the UI boundary `CameraCaptureActivity`. No covert recording attempts are made.

## Next Steps
The Camera evidence pipeline is fully functioning. The Vault correctly records real image files to private storage. The integration of audio, location, or encryption overlays will follow in subsequent steps.
