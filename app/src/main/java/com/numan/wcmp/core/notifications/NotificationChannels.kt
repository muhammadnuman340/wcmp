package com.numan.wcmp.core.notifications

/**
 * Definition of system notification channels for WCMP.
 */
object NotificationChannels {
    const val CHANNEL_PROTECTION_SERVICE = "wcmp_protection_service"
    const val CHANNEL_INTRUDER_ALERTS = "wcmp_intruder_alerts"
    const val CHANNEL_SYSTEM_AUDIT = "wcmp_system_audit"

    const val NOTIFICATION_ID_FOREGROUND_SERVICE = 1001
    const val NOTIFICATION_ID_ALERT_EVENT = 1002
}
