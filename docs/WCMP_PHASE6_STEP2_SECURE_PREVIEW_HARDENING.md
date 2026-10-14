# WCMP Phase 6 Step 2 - Secure Evidence Preview Hardening

## 1. Objective
Harden the Secure Evidence Preview implemented in Phase 6 Step 1. The focus is on robust lifecycle safety, preventing duplicate decryptions, strictly halting media playback when backgrounding, and proactively scrubbing stale cache entries upon initialization.

## 2. Audit Findings
- **Image Caching**: The step 1 `AsyncImage` Coil load was successfully replaced with standard Compose `Image(bitmap = BitmapFactory.decodeFile(...).asImageBitmap())`, keeping evidence in memory exactly as long as the composable is active, avoiding any uncontrollable disk-caching mechanisms. 
- **Media Player**: Found a missing background-lifecycle hook. 
- **Decryption Overlaps**: Found potential for duplicate requests if users tapped extremely rapidly across evidence items. 

## 3. Image Cache Strategy
- **IMPLEMENTED**: Fully bypassed Coil for secure evidence viewing. Replaced with localized `BitmapFactory` decoding directly from the temporary sandboxed file. Decoded bitmap exists exclusively inside Compose's memory space and dies with the Activity/Composable. 

## 4. Image Lifecycle
- **IMPLEMENTED**: Active decrypted files are scrubbed as soon as the ViewModel `.cleanup()` is invoked or upon `onCleared()`. 

## 5. Audio Lifecycle
- **IMPLEMENTED**: Extended `EvidencePreviewViewModel` with a strict `pausePlayback()` method. Tied UI-layer `DisposableEffect` with `LifecycleEventObserver` listening for `ON_PAUSE` and `ON_STOP` to instantly kill active playback when WCMP minimizes. 

## 6. Cancellation
- **IMPLEMENTED**: `loadEvidence` begins with `decryptionJob?.cancel()` and `cleanup()`. A new coroutine spawns for decryption, rendering previous jobs harmlessly cancelled without corrupting memory states.

## 7. Rotation/Background Behavior
- **IMPLEMENTED**: `EvidencePreviewViewModel` gracefully survives configuration changes. Temporary files and states maintain continuity during rotations. Audio appropriately halts upon hitting the `ON_PAUSE` Android background threshold. 

## 8. Temporary-File Handling
- **IMPLEMENTED**: Kept temporary artifacts inside `context.cacheDir/controlled_viewing`. Explicitly avoided passing any evidence URIs or paths outside of the host application (No `MediaStore`, no `ACTION_VIEW`, no `FileProvider`). 

## 9. Stale-File Cleanup
- **IMPLEMENTED**: Hardened the ViewModel with an `init` block that executes `decryptionService.cleanupCache()` instantly on startup, eliminating any orphaned decrypted cache entries left over from unexpected previous process deaths (e.g., OOM kills or abrupt crashes).

## 10. Integrity Handling
- **IMPLEMENTED & TESTED**: `EvidenceHealth.INTEGRITY_ERROR` guarantees no preview decryption executes, preventing manipulated data from crashing the app or surfacing corrupt states. 

## 11. KeyStore Handling
- **IMPLEMENTED & TESTED**: Re-verified that `KEY_UNAVAILABLE` triggers "KEY UNAVAILABLE" safely without silently overwriting the original ciphertext or attempting unauthorized key regenerations. 

## 12. Legacy Handling
- **IMPLEMENTED**: `LEGACY` state successfully displays "LEGACY / UNENCRYPTED". 

## 13. Location Security Wording
- **IMPLEMENTED**: Ensured no fake AES claims. Location reads raw lat/long coordinates, rendering strictly as "SECURE CAPTURE". 

## 14. Accessibility
- **IMPLEMENTED**: Preview labels correctly identify elements (`contentDescription = "Decrypted photo evidence"`, `"Pause encrypted audio evidence"`, etc.), maintaining 48dp+ tap targets across interactive playback elements. 

## 15. Performance
- **IMPLEMENTED**: Prevented duplicate overlapping decryption queries inside `loadEvidence` by rejecting duplicate IDs tracking the current state (if already `Preparing` or `Decrypting`). Decryption leverages `Dispatchers.IO` entirely. 

## 16. Tests
- **IMPLEMENTED**: Updated tests run with the new logic, passing the full suite (including `ControlledDecryptionServiceTest` and `EvidencePreviewViewModelTest`). 
- **VERIFIED**: `test` passed successfully avoiding regression.

## 17. Build
- **VERIFIED**: `assembleDebug` passes completely. 

## 18. Lint
- **VERIFIED**: `lint` passes cleanly with no newly introduced warnings. 

## 19. Device Verification
- **NOT VERIFIED**: No physical device / emulator was provided for hardware-specific assertions. 

## 20. Known Limitations
- Standard `BitmapFactory` loads the full image into memory; this does not scale to ultra-high-resolution sensors on low-memory devices without additional subsampling/downsampling parameters. 

**Final Status:** COMPLETED WITH KNOWN LIMITATIONS
