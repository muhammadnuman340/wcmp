# WCMP Phase 6 Step 1 - Secure Evidence Preview

## 1. Objective
Implement the Secure Vault evidence-preview experience identified during the Phase 5 final QA. This step replaces the placeholder preview with a real, controlled, security-conscious preview experience for camera, audio, and location evidence, ensuring that encrypted evidence is decrypted only to a short-lived temporary cache and never exposed unsafely.

## 2. Existing Security Architecture Audited
- `ControlledDecryptionService`: Evaluated and utilized. Decrypts evidence safely into a short-lived `context.cacheDir/controlled_viewing` directory and supports secure cleanup.
- `VaultEvidenceDetails`: Evaluated. Replaced the static placeholder UI with a "VIEW EVIDENCE" action triggering the new `EvidencePreviewScreen`.
- `EvidenceHealth`: Evaluated. Preview strictly respects states like `INTEGRITY_ERROR`, `KEY_UNAVAILABLE`, and `LEGACY`.
- `EncryptedEvidenceStorage`: Validated the flow of encrypted files.

## 3. Files Created
- `app/src/main/java/com/numan/wcmp/feature/vault/preview/EvidencePreviewState.kt`
- `app/src/main/java/com/numan/wcmp/feature/vault/preview/EvidencePreviewViewModel.kt`
- `app/src/main/java/com/numan/wcmp/feature/vault/preview/EvidencePreviewScreen.kt`
- `app/src/test/java/com/numan/wcmp/feature/vault/preview/EvidencePreviewViewModelTest.kt`
- `app/src/test/java/com/numan/wcmp/domain/evidence/health/ControlledDecryptionServiceTest.kt`

## 4. Files Modified
- `app/src/main/java/com/numan/wcmp/feature/vault/components/VaultEvidenceDetails.kt`

## 5. Controlled Decryption Flow
- **IMPLEMENTED**: The `EvidencePreviewViewModel` receives the UI model and checks the `EvidenceHealth`. If healthy, it launches an IO coroutine calling `ControlledDecryptionService.decryptForViewing`. This creates a temporary plaintext file. 

## 6. Camera Preview
- **IMPLEMENTED**: Uses `Coil` (`AsyncImage`) to render the decrypted private JPEG safely scaled within the UI bounds. Avoids memory exhaustion by not attempting full-res unnecessary loads.

## 7. Audio Playback
- **IMPLEMENTED**: Utilizes `MediaPlayer` managed securely inside the ViewModel. Supports Play, Pause, Seek, and shows current playback position vs duration. Audio does not autoplay.

## 8. Location Preview
- **IMPLEMENTED**: Location uses plaintext metadata mapping from Room. Displays exact coordinates and accuracy without false claims of AES-GCM encryption. It displays "SECURE CAPTURE".

## 9. Temporary Storage
- **IMPLEMENTED**: `ControlledDecryptionService` places output inside `context.cacheDir/controlled_viewing` with randomized UUIDs to prevent collisions.

## 10. Cleanup
- **IMPLEMENTED**: When the `EvidencePreviewViewModel` is cleared, or `cleanup()` is called (e.g., when closing the preview overlay), it releases the MediaPlayer and deletes the active decrypted file. It also calls the service-level cache cleanup as a fallback.

## 11. Security-State Handling
- **IMPLEMENTED**: Prevents decryption for `INTEGRITY_ERROR`, `KEY_UNAVAILABLE`, `MISSING_FILE`, and `MIGRATION_REQUIRED`. Correctly identifies `LEGACY` files. Displays clear visual errors for each.

## 12. Tests
- **IMPLEMENTED**: Added JUnit tests covering the ViewModel states (`LocationReady`, `IntegrityError`, `KeyUnavailable`, `ImageReady`) and `ControlledDecryptionService` cleanup logic.

## 13. Build
- **VERIFIED**: `assembleDebug` passes. 

## 14. Lint
- **VERIFIED**: `lint` passes.

## 15. Device Verification
- **NOT VERIFIED**: Code verified via static analysis and unit tests. Physical device testing is pending manual QA.

## 16. Known Limitations
- Coil might cache the temporary image in memory while active; once the composable is removed, cache eviction policies apply.
- Video playback is not implemented in this step.

**Status: COMPLETED**
