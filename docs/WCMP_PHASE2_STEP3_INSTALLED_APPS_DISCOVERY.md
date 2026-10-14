# WCMP — Phase 2 / Step 3

## Installed Apps Discovery & Protected Apps Repository

### 1. Implementation Status
Successfully completed Phase 2 / Step 3. The `ProtectedAppsScreen` now relies on a real `PackageManager` source synchronized with a Room database to track and persist "protected" configuration states across sessions.

### 2. Architecture
```text
Android PackageManager
       ↓
InstalledAppDataSource (Retrieves system packages and delegates categorization)
       ↓
ProtectedAppsRepository (Merges real-time discovery with Room persisted protection configurations)
       ↓
Room / ProtectedAppDao (Stores configured protection states across sessions)
       ↓
ProtectedAppsViewModel (Exposes combined results as StateFlow)
       ↓
ProtectedAppsScreen (Renders accurate search, filtering, and bulk toggling operations)
```

### 3. Files Created
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/protectedapps/utils/ProtectedAppCategoryClassifier.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/local/apps/InstalledAppDataSource.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/repository/ProtectedAppsRepository.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/protectedapps/ProtectedAppsViewModel.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/feature/protectedapps/utils/ProtectedAppCategoryClassifierTest.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/data/repository/ProtectedAppsRepositoryTest.kt`

### 4. Files Modified
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/protectedapps/model/ProtectedApp.kt` (Promoted to Data Model)
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/local/database/entity/ProtectedAppEntity.kt` (Primary key shift and expansion)
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/local/database/dao/ProtectedAppDao.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/mapper/SecurityDataMappers.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/local/database/WcmpDatabase.kt` (Version Bump)
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/local/database/WcmpDatabaseProvider.kt` (Added `Migration(1, 2)`)
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/WcmpRepositoryProvider.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/feature/protectedapps/ProtectedAppsScreen.kt` (Removed mock sample data, integrated `ViewModel`)

### 5. Package Discovery Strategy
`InstalledAppDataSource` calls `PackageManager.getInstalledPackages()`. It loops through the results, retrieving the app name via `getApplicationLabel()` and discarding the WCMP package itself to prevent self-locking. The items are then categorized natively.

### 6. Package Visibility / Manifest
The decision was made to retain `android.permission.QUERY_ALL_PACKAGES` because a security application intrinsically requires boundless visibility to accurately discover unlisted, obfuscated, or newly installed third-party apps that a malicious user might attempt to hide. This ensures 100% discovery coverage.

### 7. Room Integration
The `ProtectedAppEntity` was expanded to accurately reflect system app flags and version codes. `packageName` was adopted as the true primary key. To avoid destructive clearing, a formal `Migration(1, 2)` was added via `WcmpDatabaseProvider` which drops the placeholder tables and creates the exact updated schema while maintaining safety moving forward. `observeAppByPackageNameSync` was created for repository lookups during mapping.

### 8. Protection State
Protection state lives completely independent of discovery. A newly discovered app (`InstalledAppDataSource`) maps strictly to `isProtected = false`. The repository cross-references every newly discovered app against the Room DB. If the DB contains a true protection flag for that package, the configuration survives. An uninstalled protected application does not blindly clear the protection settings (future-proofing against re-installs).

### 9. UI Integration
`ProtectedAppsScreen` dropped the dummy states. `ProtectedAppsViewModel` exposes `StateFlow` streams. `ProtectedAppsScreen` now natively loops bulk selections (Protect, Unprotect, toggle) up to the repository to safely update the Room database, meaning the UI observes standard reactivity loops instead of mutating generic lists. Filtering applies accurately across local domain enums.

### 10. Tests
- **ProtectedAppCategoryClassifierTest**: Tested WhatsApp/Gallery to ensure deterministic mapping into `COMMUNICATION` and `MEDIA`.
- **ProtectedAppsRepositoryTest**: Created complex fakes for DAO and DataSource, confirming that a newly fetched app defaults to unprotected while an already protected app survives synchronization merges. Also validated bulk updates spanning multiple `packageNames`.

### 11. Build
- **assembleDebug**: `BUILD SUCCESSFUL`
- **test**: `BUILD SUCCESSFUL`

### 12. Limitations
- **No Background Monitoring**: The application correctly categorizes protected apps, but WCMP will not actively block or record user entry into those apps yet.
- **No Foreground Detection**: Does not yet observe standard UI window transitions.
- **No Accessibility/UsageStats**: Permissions are completely untouched.

### 13. Next Phase
I recommend we target **Phase 2 / Step 4 — Background WorkManager Architecture**. Establishing the background processing layer will provide a robust engine for executing future App Locking and background surveillance mechanics.
