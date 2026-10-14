# WCMP — Phase 3 / Step 1

## Security Engine & Foreground Monitoring Foundation

### 1. Implementation Status
Completed Phase 3 / Step 1 successfully. The security engine and real-time foreground monitoring foundation have been fully implemented without violating strict privacy and capability boundaries.

### 2. Selected Foreground Observation Mechanism
**AccessibilityService** was selected as the foreground observation mechanism.
**Architectural Reason**: Unlike `UsageStatsManager` which is subject to system polling latency and strict quota limitations, an `AccessibilityService` observing `TYPE_WINDOW_STATE_CHANGED` provides real-time event-driven package transitions. This is functionally necessary for a responsive App Lock engine (to prevent a user from interacting with a protected app before the lock screen deploys). 

### 3. Architecture
```text
Android AccessibilityService (WcmpAccessibilityService)
        ↓
AccessibilityForegroundMonitor (ForegroundMonitor)
        ↓
SecurityEngine (Manages state, deduplication, lifecycle)
        ↓
SecurityEventProcessor (Translates transition to domain event)
        ↓
SecurityRepository (Database interactions)
        ↓
Room (Persistence)
        ↓
EventsScreen (UI)
```

### 4. Files Created
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/security/ForegroundApp.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/security/ForegroundMonitorStatus.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/security/ForegroundMonitor.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/security/SecurityEngineState.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/security/SecurityEventProcessor.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/domain/security/SecurityEngine.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/monitoring/AccessibilityForegroundMonitor.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/monitoring/WcmpAccessibilityService.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/res/xml/accessibility_service_config.xml`
- `c:/numan/Numanx-projacts/wcmp/app/src/test/java/com/numan/wcmp/domain/security/SecurityEngineTest.kt`

### 5. Files Modified
- `c:/numan/Numanx-projacts/wcmp/app/src/main/res/values/strings.xml`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/AndroidManifest.xml`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/AndroidCapability.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/core/permissions/SpecialAccessManager.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/data/WcmpRepositoryProvider.kt`
- `c:/numan/Numanx-projacts/wcmp/app/src/main/java/com/numan/wcmp/WcmpApplication.kt`

### 6. Capability Integration
The existing Phase 2 capability abstraction (`AndroidCapability.ACCESSIBILITY_SERVICE`) was updated:
- Marked `isImplemented = true`.
- Display name updated to "Foreground Monitoring".
- Integrated seamlessly with `SpecialAccessManager` using proper `Settings.Secure` system checks (`ENABLED_ACCESSIBILITY_SERVICES`), allowing standard project-wide diagnostics without duplicating abstraction code.

### 7. Lifecycle Behavior
The monitor (`AccessibilityForegroundMonitor`) tracks two disparate states to determine true readiness:
- `isRequestedToRun` (Controlled by `SecurityEngine.start()` / `.stop()`)
- `isServiceBound` (Controlled by OS lifecycle `onServiceConnected()` / `onUnbind()`)
The `SecurityEngine` utilizes `ForegroundMonitorStatus.REQUIRES_ACCESS` logically. Currently, for testing the engine boundaries, it is started generically inside `WcmpApplication.kt`, but it strictly operates locally and emits zero events if the OS lacks binding.

### 8. Protected-App Detection
When `SecurityEngine` receives an observed transition from the monitor, it safely queries the `ProtectedAppsRepository` logic directly (via `observeProtectedAppsOnly().first()`). Unprotected apps or `com.numan.wcmp` natively bypass processing instantly.

### 9. Duplicate Prevention
Session-based deduplication is enforced aggressively inside `SecurityEngine`. Transitioning inside an active protected app (e.g., launching an internal activity) triggers no additional alerts. A user must transition back out (to launcher or another app), clearing the `currentProtectedSessionPackage`, before a new transition triggers another discrete alert.

### 10. Security Events
Upon verifying an access rule, `SecurityEventProcessor` generates a `SecurityEventEntity` categorized as `type = "PROTECTED_APP_ACCESS"` with `severity = "HIGH"`. This flows into `SecurityRepository` exactly matching the legacy architectural pipeline ensuring the UI naturally reflects it downstream.

### 11. UI Changes
No disruptive redesigns were applied. `DiagnosticsCard.kt` dynamically populates the capabilities UI, leveraging the updated `AndroidCapability` names natively to show `Foreground Monitoring` accurately as `READY`, `REQUIRES ACCESS`, or `NOT IMPLEMENTED`.

### 12. Manifest Changes
```xml
<service
    android:name=".core.monitoring.WcmpAccessibilityService"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/accessibility_service_config" />
</service>
```

### 13. Tests
- **SecurityEngineTest.kt**: Fully covers six distinct critical security use cases without relying on Mockito or an active Android framework:
  - Unprotected app transition (ignored)
  - Protected app transition (fires event)
  - Duplicate foreground updates (filtered strictly to 1 event)
  - Leave and return session logic (fires 2 distinct events)
  - Multiple app switches (fires specific respective events)
  - WCMP core app transition (ignored explicitly)
All unit tests passed.

### 14. Build Results
- **assembleDebug**: `BUILD SUCCESSFUL`
- **test**: `BUILD SUCCESSFUL`

### 15. Security & Privacy Boundaries
**CRITICAL**: This phase strictly implements real-time observation and architecture ONLY.
It explicitly **DOES NOT** implement:
- App locking mechanism or blocking UI.
- Credential capture (PIN/passwords).
- Screen capturing / UI scraping.
- Camera image evidence.
- Audio recording / stealth surveillance.
- Location tracking / Network interception.
- Deceptive overlays or fake crash screens.

### 16. Limitations
Platform limitations explicitly dictate that accessibility services require active, user-initiated permission grants via Android system settings. If disabled, WCMP gracefully tracks its `REQUIRES_ACCESS` status transparently. The `canRetrieveWindowContent` flag is explicitly locked to `false` permanently.

### 17. Recommended Next Step
Proceed to **Phase 3 / Step 2 — App Locking Controller & UI Component**. This will involve consuming the generated `PROTECTED_APP_ACCESS` state intelligently to rapidly inflate a blocking UI (e.g., a biometric or PIN verification screen) over the authorized foreground application transition before it can be actively manipulated by the user.
