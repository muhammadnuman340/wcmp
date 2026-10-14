# WCMP Phase 4 / Step 4 — Location Evidence Integration

## Objective
Implement a safe, explicit, foreground-only one-shot location evidence capture capability for WCMP, following the architectural boundaries established in Phase 4 Steps 2 (Camera) and 3 (Audio).

## Architecture & Implementation Details

### 1. Database & Domain Models
- Added `play-services-location:21.3.0` to Gradle configuration.
- Extended `EvidenceRecord` domain model with nullable location properties: `latitude`, `longitude`, `accuracyMeters`, and `altitude`.
- Extended `VaultEvidenceEntity` with corresponding nullable location properties.
- Updated `WcmpDatabase` to version 3 and provided a non-destructive `MIGRATION_2_3` in `WcmpDatabaseProvider` using `ALTER TABLE` to preserve existing data safely.
- Added `LOCATION_STAMP` type handling in `DefaultEvidenceStorage` and `SecurityDataMappers`.
- Created a new `VaultEvidenceType.LOCATION` mapping in `VaultEvidence.kt`.

### 2. Evidence Policy & Trigger
- Added `locationEvidenceEnabled` setting to `WcmpPreferences`.
- Updated `EvidencePolicy` to respect the `locationEnabledFlow` when resolving `EvidenceType.LOCATION_STAMP` requests.

### 3. Capture Strategy & Session Bridge
- Created `AndroidLocationCaptureStrategy` which verifies `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` availability and dispatches an Intent to start the UI.
- Implemented `LocationCaptureSession`, adapting the Singleton asynchronous continuation approach used for camera and audio. This maintains Android's strict background execution constraints while tying foreground execution back to the `EvidenceCaptureCoordinator`.

### 4. Foreground Capture UI
- Designed `LocationCaptureScreen` using Jetpack Compose, featuring:
  - Material 3 theming (Deep Navy Black, Cyan Accent).
  - Explicit UI state progression (`Idle`, `Acquiring`, `Success`, `Failed`, `PermissionRequired`, `Cancelled`).
  - Animated acquiring state and comprehensive user-facing text clarifying exactly what is being captured.
- Configured `LocationCaptureViewModel` leveraging `FusedLocationProviderClient.getCurrentLocation()` for high-accuracy one-shot resolution with a strict 15,000ms timeout.
- Handled runtime permissions safely using `ActivityResultContracts.RequestMultiplePermissions`.

### 5. Diagnostics & Vault
- Added `LOCATION_STAMP` pipeline testing to `EvidenceDiagnosticsCard.kt`.
- Verified and displayed `Location Capture` capability status in the Diagnostics UI.
- Updated `VaultEvidenceCard` to use a specific icon (`Icons.Default.Place`) for `VaultEvidenceType.LOCATION`.
- Updated `VaultDetailsSheet` to display formatted Latitude, Longitude, and Accuracy when applicable.

## Design Constraints Honored
- **No Background Tracking**: The location is fetched once in the foreground through an explicit user-driven Activity.
- **No Play Services Maps UI**: No map SDKs or `MapView` dependencies were added.
- **Strict Data Retention**: Reused the existing evidence storage boundary without bypassing Room policies.
- **No Fake Data**: True location objects and exceptions are propagated properly from `FusedLocationProviderClient`.
