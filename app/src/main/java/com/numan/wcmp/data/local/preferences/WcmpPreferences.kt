package com.numan.wcmp.data.local.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WcmpPreferences(private val context: Context) {

    companion object {
        val PROTECTION_MODE = stringPreferencesKey("protection_mode")
        val AUTO_PROTECTION = booleanPreferencesKey("auto_protection")
        val MOTION_DETECTION = booleanPreferencesKey("motion_detection")
        val WRONG_PIN = booleanPreferencesKey("wrong_pin")
        val APP_ACCESS = booleanPreferencesKey("app_access")
        val NOTIFICATION_ALERTS = booleanPreferencesKey("notification_alerts")
        val ANIMATIONS = booleanPreferencesKey("animations")
        val THEME = stringPreferencesKey("theme")
        val CAMERA_EVIDENCE = booleanPreferencesKey("camera_evidence")
        val AUDIO_EVIDENCE = booleanPreferencesKey("audio_evidence")
        val LOCATION_EVIDENCE = booleanPreferencesKey("location_evidence")
    }

    val protectionMode: Flow<String> = context.dataStore.data.map { it[PROTECTION_MODE] ?: "Quick Protect" }
    suspend fun setProtectionMode(mode: String) {
        context.dataStore.edit { it[PROTECTION_MODE] = mode }
    }

    val autoProtectionEnabled: Flow<Boolean> = context.dataStore.data.map { it[AUTO_PROTECTION] ?: false }
    suspend fun setAutoProtection(enabled: Boolean) {
        context.dataStore.edit { it[AUTO_PROTECTION] = enabled }
    }

    val motionDetectionEnabled: Flow<Boolean> = context.dataStore.data.map { it[MOTION_DETECTION] ?: false }
    suspend fun setMotionDetection(enabled: Boolean) {
        context.dataStore.edit { it[MOTION_DETECTION] = enabled }
    }

    val wrongPinEnabled: Flow<Boolean> = context.dataStore.data.map { it[WRONG_PIN] ?: false }
    suspend fun setWrongPin(enabled: Boolean) {
        context.dataStore.edit { it[WRONG_PIN] = enabled }
    }

    val appAccessEnabled: Flow<Boolean> = context.dataStore.data.map { it[APP_ACCESS] ?: false }
    suspend fun setAppAccess(enabled: Boolean) {
        context.dataStore.edit { it[APP_ACCESS] = enabled }
    }

    val notificationAlertsEnabled: Flow<Boolean> = context.dataStore.data.map { it[NOTIFICATION_ALERTS] ?: true }
    suspend fun setNotificationAlerts(enabled: Boolean) {
        context.dataStore.edit { it[NOTIFICATION_ALERTS] = enabled }
    }

    val animationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[ANIMATIONS] ?: true }
    suspend fun setAnimations(enabled: Boolean) {
        context.dataStore.edit { it[ANIMATIONS] = enabled }
    }

    val cameraEvidenceEnabled: Flow<Boolean> = context.dataStore.data.map { it[CAMERA_EVIDENCE] ?: false }
    suspend fun setCameraEvidence(enabled: Boolean) {
        context.dataStore.edit { it[CAMERA_EVIDENCE] = enabled }
    }

    val audioEvidenceEnabled: Flow<Boolean> = context.dataStore.data.map { it[AUDIO_EVIDENCE] ?: false }
    suspend fun setAudioEvidence(enabled: Boolean) {
        context.dataStore.edit { it[AUDIO_EVIDENCE] = enabled }
    }

    val locationEvidenceEnabled: Flow<Boolean> = context.dataStore.data.map { it[LOCATION_EVIDENCE] ?: false }
    suspend fun setLocationEvidence(enabled: Boolean) {
        context.dataStore.edit { it[LOCATION_EVIDENCE] = enabled }
    }

    val theme: Flow<String> = context.dataStore.data.map { it[THEME] ?: "System Default" }
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME] = theme }
    }
}
