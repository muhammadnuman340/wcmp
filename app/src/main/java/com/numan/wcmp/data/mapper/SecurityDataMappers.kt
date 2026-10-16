package com.numan.wcmp.data.mapper

import com.numan.wcmp.data.local.database.entity.ProtectedAppEntity
import com.numan.wcmp.data.local.database.entity.VaultEvidenceEntity
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType

import com.numan.wcmp.feature.protectedapps.model.ProtectedAppCategory

fun ProtectedAppEntity.toDomainModel(): ProtectedApp {
    return ProtectedApp(
        packageName = packageName,
        name = appName,
        isSystemApp = isSystemApp,
        isProtected = isProtected,
        category = try { ProtectedAppCategory.valueOf(category) } catch (e: Exception) { ProtectedAppCategory.OTHER },
        versionName = versionName,
        versionCode = versionCode
    )
}

fun ProtectedApp.toEntity(): ProtectedAppEntity {
    return ProtectedAppEntity(
        packageName = packageName,
        appName = name,
        isSystemApp = isSystemApp,
        category = category.name,
        isProtected = isProtected,
        versionName = versionName,
        versionCode = versionCode,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

fun VaultEvidenceEntity.toDomainModel(): VaultEvidence {
    return VaultEvidence(
        id = id,
        title = title,
        type = VaultEvidenceType.valueOf(type),
        relatedEvent = relatedEventId,
        timestamp = timestamp.toString(), // Simplified mapping for UI
        size = size,
        category = VaultCategory.valueOf(category),
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        altitude = altitude,
        isEncrypted = isEncrypted,
        encryptionVersion = encryptionVersion,
        sessionId = sessionId
    )
}

fun VaultEvidence.toEntity(): VaultEvidenceEntity {
    return VaultEvidenceEntity(
        id = id,
        title = title,
        type = type.name,
        relatedEventId = relatedEvent,
        timestamp = System.currentTimeMillis(), // Fallback for sample mapping
        size = size,
        category = category.name,
        storageStatus = "NOT_STORED",
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        altitude = altitude,
        isEncrypted = isEncrypted,
        encryptionVersion = encryptionVersion,
        sessionId = sessionId
    )
}

fun com.numan.wcmp.data.local.database.entity.SecurityEventEntity.toDomainModel(): com.numan.wcmp.domain.events.SecurityEvent {
    val triggerType = try {
        com.numan.wcmp.domain.events.EventTriggerType.valueOf(trigger)
    } catch (e: Exception) {
        com.numan.wcmp.domain.events.EventTriggerType.APP_UNAUTHORIZED_LAUNCH
    }
    return com.numan.wcmp.domain.events.SecurityEvent(
        id = id,
        timestamp = timestamp,
        triggerType = triggerType,
        summary = "$title: $description",
        isConfirmedIntruder = severity.equals("CRITICAL", ignoreCase = true) || severity.equals("HIGH", ignoreCase = true)
    )
}
