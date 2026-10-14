package com.numan.wcmp.core.monitoring

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class WcmpAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityForegroundMonitor.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName
            if (packageName != null) {
                AccessibilityForegroundMonitor.onForegroundPackageChanged(packageName)
            }
        }
    }

    override fun onInterrupt() {
        // Required by AccessibilityService but not specifically used by our monitor
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        AccessibilityForegroundMonitor.onServiceDisconnected()
        return super.onUnbind(intent)
    }
}
