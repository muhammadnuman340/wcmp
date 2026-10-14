package com.numan.wcmp.data.repository

import com.numan.wcmp.data.local.database.dao.ProtectedAppDao
import com.numan.wcmp.data.local.database.dao.SecurityEventDao
import com.numan.wcmp.data.local.database.dao.VaultEvidenceDao
import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.data.local.preferences.WcmpPreferences
import com.numan.wcmp.data.mapper.toDomainModel
import com.numan.wcmp.data.mapper.toEntity
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import com.numan.wcmp.feature.vault.model.VaultEvidence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SecurityRepository {
    fun observeSecurityEvents(): Flow<List<SecurityEventEntity>>
    fun observeRecentEvents(limit: Int): Flow<List<SecurityEventEntity>>
    fun getSecurityEvent(id: String): Flow<SecurityEventEntity?>
    suspend fun insertSecurityEvent(event: SecurityEventEntity)
    suspend fun deleteSecurityEvent(id: String)

    fun observeVaultEvidence(): Flow<List<VaultEvidence>>
    fun getVaultEvidence(id: String): Flow<VaultEvidence?>
    suspend fun insertVaultEvidence(evidence: VaultEvidence)
    suspend fun deleteVaultEvidence(id: String)
    suspend fun deleteTestEvidence()

    fun observeProtectedApps(): Flow<List<ProtectedApp>>
    fun observeProtectedAppsOnly(): Flow<List<ProtectedApp>>
    suspend fun updateProtectedApp(app: ProtectedApp)

    val protectionMode: Flow<String>
    suspend fun setProtectionMode(mode: String)

    val autoProtectionEnabled: Flow<Boolean>
    suspend fun setAutoProtection(enabled: Boolean)

    val motionDetectionEnabled: Flow<Boolean>
    suspend fun setMotionDetection(enabled: Boolean)

    val notificationAlertsEnabled: Flow<Boolean>
    suspend fun setNotificationAlerts(enabled: Boolean)

    val animationsEnabled: Flow<Boolean>
    suspend fun setAnimations(enabled: Boolean)

    val cameraEvidenceEnabled: Flow<Boolean>
    suspend fun setCameraEvidence(enabled: Boolean)

    val audioEvidenceEnabled: Flow<Boolean>
    suspend fun setAudioEvidence(enabled: Boolean)
}

class DefaultSecurityRepository(
    private val securityEventDao: SecurityEventDao,
    private val vaultEvidenceDao: VaultEvidenceDao,
    private val protectedAppDao: ProtectedAppDao,
    private val preferences: WcmpPreferences
) : SecurityRepository {
    // --- Security Events ---
    override fun observeSecurityEvents(): Flow<List<SecurityEventEntity>> = securityEventDao.observeAllEvents()
    override fun observeRecentEvents(limit: Int): Flow<List<SecurityEventEntity>> = securityEventDao.observeRecentEvents(limit)
    override fun getSecurityEvent(id: String): Flow<SecurityEventEntity?> = securityEventDao.observeEventById(id)

    override suspend fun insertSecurityEvent(event: SecurityEventEntity) {
        securityEventDao.insertEvent(event)
    }
    
    override suspend fun deleteSecurityEvent(id: String) {
        securityEventDao.deleteEvent(id)
    }

    // --- Vault Evidence ---
    override fun observeVaultEvidence(): Flow<List<VaultEvidence>> {
        return vaultEvidenceDao.observeAllEvidence().map { list ->
            list.map { it.toDomainModel() }
        }
    }
    
    override fun getVaultEvidence(id: String): Flow<VaultEvidence?> {
        return vaultEvidenceDao.observeEvidenceById(id).map { it?.toDomainModel() }
    }

    override suspend fun insertVaultEvidence(evidence: VaultEvidence) {
        vaultEvidenceDao.insertEvidence(evidence.toEntity())
    }

    override suspend fun deleteVaultEvidence(id: String) {
        vaultEvidenceDao.deleteEvidence(id)
    }

    override suspend fun deleteTestEvidence() {
        vaultEvidenceDao.deleteTestEvidence()
    }

    // --- Protected Apps ---
    override fun observeProtectedApps(): Flow<List<ProtectedApp>> {
        return protectedAppDao.observeAllProtectedApps().map { list ->
            list.map { it.toDomainModel() }
        }
    }
    
    override fun observeProtectedAppsOnly(): Flow<List<ProtectedApp>> {
        return protectedAppDao.observeProtectedAppsOnly().map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun updateProtectedApp(app: ProtectedApp) {
        protectedAppDao.insertApp(app.toEntity()) // Uses REPLACE conflict strategy
    }

    // --- Preferences ---
    override val protectionMode: Flow<String> = preferences.protectionMode
    override suspend fun setProtectionMode(mode: String) = preferences.setProtectionMode(mode)

    override val autoProtectionEnabled: Flow<Boolean> = preferences.autoProtectionEnabled
    override suspend fun setAutoProtection(enabled: Boolean) = preferences.setAutoProtection(enabled)

    override val motionDetectionEnabled: Flow<Boolean> = preferences.motionDetectionEnabled
    override suspend fun setMotionDetection(enabled: Boolean) = preferences.setMotionDetection(enabled)

    override val notificationAlertsEnabled: Flow<Boolean> = preferences.notificationAlertsEnabled
    override suspend fun setNotificationAlerts(enabled: Boolean) = preferences.setNotificationAlerts(enabled)

    override val animationsEnabled: Flow<Boolean> = preferences.animationsEnabled
    override suspend fun setAnimations(enabled: Boolean) = preferences.setAnimations(enabled)

    override val cameraEvidenceEnabled: Flow<Boolean> = preferences.cameraEvidenceEnabled
    override suspend fun setCameraEvidence(enabled: Boolean) = preferences.setCameraEvidence(enabled)

    override val audioEvidenceEnabled: Flow<Boolean> = preferences.audioEvidenceEnabled
    override suspend fun setAudioEvidence(enabled: Boolean) = preferences.setAudioEvidence(enabled)
}
