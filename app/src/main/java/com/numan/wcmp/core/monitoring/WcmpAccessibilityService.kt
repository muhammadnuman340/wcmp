package com.numan.wcmp.core.monitoring

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityWindowInfo
import android.view.inputmethod.InputMethodManager

class WcmpAccessibilityService : AccessibilityService() {

    private val ignoredSystemPackages = setOf(
        "com.android.systemui",
        "com.google.android.permissioncontroller",
        "com.android.permissioncontroller",
        "android"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityForegroundMonitor.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED
        ) {
            return
        }

        val rawPackage = event.packageName?.toString()
        if (rawPackage != null) {
            if (isIgnoredOverlayOrIme(rawPackage, event.className?.toString())) {
                return
            }
            AccessibilityForegroundMonitor.onForegroundPackageChanged(
                packageName = rawPackage,
                className = event.className?.toString()
            )
            return
        }

        // Fallback for TYPE_WINDOWS_CHANGED (e.g., split-screen transitions)
        val activeWindowPkg = windows
            ?.firstOrNull { it.type == AccessibilityWindowInfo.TYPE_APPLICATION && it.isFocused }
            ?.root?.packageName?.toString()

        if (activeWindowPkg != null && !isIgnoredOverlayOrIme(activeWindowPkg, null)) {
            AccessibilityForegroundMonitor.onForegroundPackageChanged(
                packageName = activeWindowPkg,
                className = null
            )
        }
    }

    private fun isIgnoredOverlayOrIme(packageName: String, className: String?): Boolean {
        if (packageName in ignoredSystemPackages) return true

        // Ignore active Input Method Editors (soft keyboards)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        val isIme = imm?.enabledInputMethodList?.any { it.packageName == packageName } == true
        if (isIme) return true

        // Ignore WCMP's own transient lock overlay Activity so it does not evict the active protected app session
        if (packageName == this.packageName &&
            className == "com.numan.wcmp.feature.applock.AppLockActivity"
        ) {
            return true
        }

        return false
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onUnbind(intent: Intent?): Boolean {
        AccessibilityForegroundMonitor.onServiceDisconnected()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        AccessibilityForegroundMonitor.onServiceDisconnected()
        super.onDestroy()
    }
}
