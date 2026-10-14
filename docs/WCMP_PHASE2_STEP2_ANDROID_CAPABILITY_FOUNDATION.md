# WCMP — Phase 2 / Step 2

## Android Capability & Permission Foundation

### 1. Implementation Status
Successfully completed Phase 2 / Step 2. Established the Android Capability & Permission abstraction layer to handle Runtime Permissions and Special Access checks cleanly without cluttering Compose logic or automatically prompting the user for permissions.

### 2. Capability Architecture
```text
UI (Settings Diagnostics / Capabilities Flow)
 ↓
CapabilityRepository (Exposes StateFlow)
 ↓
CapabilityManager (Aggregates status across strategies)
 ├── RuntimePermissionManager (Handles standard ContextCompat checks)
 └── SpecialAccessManager (Handles AppOps and Settings intents)
 ↓
Android capability APIs (ContextCompat, AppOpsManager)
```

### 3. Files Created
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/AndroidCapability.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/CapabilityStatus.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/CapabilityState.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/RuntimePermissionManager.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/SpecialAccessManager.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/CapabilityManager.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/CapabilityRepository.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/CapabilityViewModel.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/core/permissions/CapabilityManagerTest.kt`

### 4. Files Modified
- `c:/numan/Numanx-projacts/wcmp/app/src/main/AndroidManifest.xml`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/WcmpRepositoryProvider.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/settings/SettingsScreen.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/settings/components/DiagnosticsCard.kt`

### 5. Runtime Permissions
The `RuntimePermissionManager` inspects:
- `CAMERA`
- `MICROPHONE`
- `LOCATION` (currently marked NOT IMPLEMENTED)
- `NOTIFICATIONS` (Aware of Android 13/Tiramisu distinctions)

### 6. Special Access
The `SpecialAccessManager` inspects:
- `USAGE_ACCESS` (AppOpsManager check supporting modern Android 10+ constraints safely)
- `ACCESSIBILITY_SERVICE` (Marked as NOT IMPLEMENTED)

### 7. Manifest Changes
Added minimal foundational permissions specifically for the required capabilities:
- `android.permission.CAMERA`
- `android.permission.RECORD_AUDIO`
- `android.permission.ACCESS_FINE_LOCATION`
- `android.permission.ACCESS_COARSE_LOCATION`
- `android.permission.POST_NOTIFICATIONS`
- `android.permission.QUERY_ALL_PACKAGES`

### 8. API-Level Handling
- Implemented `Build.VERSION.SDK_INT` check for `NOTIFICATIONS` prior to Android 13 (Returns `GRANTED` without permission requirements).
- Used `unsafeCheckOpNoThrow` dynamically for API 29+ `USAGE_ACCESS` inspection, while preserving older version fallback (`checkOpNoThrow`).

### 9. Settings Integration
`DiagnosticsCard.kt` was modified to render live states fed from the `CapabilityViewModel`. Standard permissions demonstrate `DENIED` by default while unavailable capabilities cleanly read `NOT_IMPLEMENTED`.

### 10. Security Boundaries
**CRITICAL LIMITATION**: A capability reported as `GRANTED` solely reflects operating system authorization. It does NOT imply WCMP's actual surveillance mechanisms (such as capturing photos or persisting background logs) are functioning.

### 11. Tests
Unit tests created using lightweight Fakes (because Mockito was explicitly avoided to respect clean project constraints).
- Verified catalog size matching enum entries.
- Validated graceful status downgrading for unimplemented features.

### 12. Build
Build passed cleanly.
`$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; ./gradlew assembleDebug`
`$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"; ./gradlew test`

### 13. Warnings
- The Android capability system exposes `DENIED` when `NOT_REQUESTED`. Due to Android limitations, differentiating between the two requires a UI Activity context (via `shouldShowRequestPermissionRationale`). This distinction is deferred to the future permissions UI component to keep the Repository layer pure.

### 14. Deferred Capabilities
The following capabilities and features remain intentionally unconfigured and deferred:
- PackageManager application scanning
- AccessibilityService definition and background thread
- UsageStatsManager functional monitoring engine
- Actual Camera image capturing
- Video / Microphone recording
- App Locking constraints
- Cloud Sync

### 15. Next Recommended Phase
I recommend we target **Phase 2 / Step 3 — Background WorkManager & Service Architecture** or begin implementing **PackageManager Application Discovery** (to resolve the `ProtectedAppDao`'s reliance on sample data).
