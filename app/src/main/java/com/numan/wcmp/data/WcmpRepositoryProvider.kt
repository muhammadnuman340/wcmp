package com.numan.wcmp.data

import android.content.Context
import com.numan.wcmp.data.local.database.WcmpDatabaseProvider
import com.numan.wcmp.data.local.preferences.WcmpPreferences
import com.numan.wcmp.data.repository.SecurityRepository

import com.numan.wcmp.core.permissions.CapabilityManager
import com.numan.wcmp.core.permissions.CapabilityRepository
import com.numan.wcmp.core.permissions.RuntimePermissionManager
import com.numan.wcmp.core.permissions.DefaultRuntimePermissionManager
import com.numan.wcmp.core.permissions.SpecialAccessManager
import com.numan.wcmp.core.permissions.DefaultSpecialAccessManager
import com.numan.wcmp.data.local.apps.InstalledAppDataSource
import com.numan.wcmp.data.local.apps.DefaultInstalledAppDataSource
import com.numan.wcmp.data.repository.ProtectedAppsRepository
import com.numan.wcmp.data.repository.DefaultProtectedAppsRepository
import com.numan.wcmp.domain.protection.AppLockController
import com.numan.wcmp.domain.protection.DefaultAppLockPolicy
import com.numan.wcmp.domain.protection.DefaultAppLockPresenter
import com.numan.wcmp.domain.security.SecurityEngine
import com.numan.wcmp.domain.security.SecurityEventProcessor
import com.numan.wcmp.core.monitoring.AccessibilityForegroundMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.numan.wcmp.domain.evidence.EvidencePolicy
import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.DefaultEvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.core.evidence.AndroidCameraCaptureStrategy
import com.numan.wcmp.core.evidence.AndroidAudioCaptureStrategy
import com.numan.wcmp.core.evidence.AndroidLocationCaptureStrategy
import com.numan.wcmp.data.repository.DefaultSecurityRepository

object WcmpRepositoryProvider {
    @Volatile
    private var securityRepository: SecurityRepository? = null

    @Volatile
    private var capabilityRepository: CapabilityRepository? = null

    @Volatile
    private var protectedAppsRepository: ProtectedAppsRepository? = null

    @Volatile
    private var securityEngine: SecurityEngine? = null

    @Volatile
    private var appLockController: AppLockController? = null

    @Volatile
    private var evidenceCaptureCoordinator: EvidenceCaptureCoordinator? = null

    @Volatile
    private var evidenceIntegrityService: com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService? = null

    @Volatile
    private var securitySessionManager: com.numan.wcmp.domain.security.SecuritySessionManager? = null

    fun getSecuritySessionManager(): com.numan.wcmp.domain.security.SecuritySessionManager {
        return securitySessionManager ?: synchronized(this) {
            val manager = com.numan.wcmp.domain.security.SecuritySessionManager()
            securitySessionManager = manager
            manager
        }
    }

    fun getEvidenceIntegrityService(context: Context): com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService {
        return evidenceIntegrityService ?: synchronized(this) {
            val keyManager = com.numan.wcmp.core.security.evidence.DefaultEvidenceKeyManager()
            val directoryProvider = com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider(context.applicationContext)
            val healthEvaluator = com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator(context.applicationContext.filesDir, keyManager)
            val service = com.numan.wcmp.domain.evidence.health.DefaultEvidenceIntegrityService(directoryProvider, keyManager, healthEvaluator)
            evidenceIntegrityService = service
            service
        }
    }

    fun getEvidenceCaptureCoordinator(context: Context): EvidenceCaptureCoordinator {
        return evidenceCaptureCoordinator ?: synchronized(this) {
            val prefs = WcmpPreferences(context)
            val policy = EvidencePolicy(
                cameraEnabledFlow = prefs.cameraEvidenceEnabled,
                audioEnabledFlow = prefs.audioEvidenceEnabled,
                locationEnabledFlow = prefs.locationEvidenceEnabled
            )
            val storage = com.numan.wcmp.core.security.evidence.EncryptedEvidenceStorage(
                context.applicationContext.filesDir,
                getSecurityRepository(context)
            )
            val strategies = mapOf(
                EvidenceType.FRONT_CAMERA_PHOTO to AndroidCameraCaptureStrategy(context.applicationContext),
                EvidenceType.AUDIO_CLIP to AndroidAudioCaptureStrategy(context.applicationContext),
                EvidenceType.LOCATION_STAMP to AndroidLocationCaptureStrategy(context.applicationContext)
            )
            val coordinator = DefaultEvidenceCaptureCoordinator(policy, strategies, storage)
            evidenceCaptureCoordinator = coordinator
            coordinator
        }
    }

    fun getAppLockController(context: Context): AppLockController {
        return appLockController ?: synchronized(this) {
            val controller = AppLockController(
                policy = DefaultAppLockPolicy(),
                securityRepository = getSecurityRepository(context),
                presenter = DefaultAppLockPresenter(context.applicationContext),
                evidenceCoordinator = getEvidenceCaptureCoordinator(context), // We need to update AppLockController to take EvidenceCoordinator
                coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            )
            appLockController = controller
            controller
        }
    }

    fun getSecurityEngine(context: Context): SecurityEngine {
        return securityEngine ?: synchronized(this) {
            val engine = SecurityEngine(
                foregroundMonitor = AccessibilityForegroundMonitor,
                protectedAppsRepository = getProtectedAppsRepository(context),
                eventProcessor = SecurityEventProcessor(getSecurityRepository(context), getSecuritySessionManager()),
                appLockController = getAppLockController(context),
                coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
                sessionManager = getSecuritySessionManager()
            )
            securityEngine = engine
            engine
        }
    }

    // For testing purposes
    fun setProtectedAppsRepositoryForTest(repo: ProtectedAppsRepository?) {
        protectedAppsRepository = repo
    }

    fun getProtectedAppsRepository(context: Context): ProtectedAppsRepository {
        return protectedAppsRepository ?: synchronized(this) {
            val db = WcmpDatabaseProvider.getDatabase(context)
            val dataSource = DefaultInstalledAppDataSource(context.applicationContext)
            val repo = DefaultProtectedAppsRepository(
                appDataSource = dataSource,
                protectedAppDao = db.protectedAppDao()
            )
            protectedAppsRepository = repo
            repo
        }
    }

    fun getSecurityRepository(context: Context): SecurityRepository {
        return securityRepository ?: synchronized(this) {
            val db = WcmpDatabaseProvider.getDatabase(context)
            val prefs = WcmpPreferences(context)
            val repo = DefaultSecurityRepository(
                securityEventDao = db.securityEventDao(),
                vaultEvidenceDao = db.vaultEvidenceDao(),
                protectedAppDao = db.protectedAppDao(),
                preferences = prefs
            )
            securityRepository = repo
            repo
        }
    }

    fun getCapabilityRepository(context: Context): CapabilityRepository {
        return capabilityRepository ?: synchronized(this) {
            val appContext = context.applicationContext
            val runtimePermissionManager = DefaultRuntimePermissionManager(appContext)
            val specialAccessManager = DefaultSpecialAccessManager(appContext)
            val capabilityManager = CapabilityManager(runtimePermissionManager, specialAccessManager)
            val repo = CapabilityRepository(capabilityManager)
            capabilityRepository = repo
            repo
        }
    }
}
