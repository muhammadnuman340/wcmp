package com.numan.wcmp.core.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * Definition of system notification channels for WCMP.
 */
object NotificationChannels {
    const val CHANNEL_PROTECTION_SERVICE = "wcmp_protection_service"
    const val CHANNEL_INTRUDER_ALERTS = "wcmp_intruder_alerts"
    const val CHANNEL_SYSTEM_AUDIT = "wcmp_system_audit"

    const val NOTIFICATION_ID_FOREGROUND_SERVICE = 1001
    const val NOTIFICATION_ID_ALERT_EVENT = 1002

    fun createSilentSecurityNotification(context: Context): Notification {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_PROTECTION_SERVICE,
                "Security Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Silent foreground security monitoring and capture"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(context, CHANNEL_PROTECTION_SERVICE)
            .setContentTitle("Security Protection Active")
            .setContentText("Monitoring device security")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}
