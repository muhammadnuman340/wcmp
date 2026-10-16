package com.numan.wcmp.data

import android.content.Context
import com.numan.wcmp.core.evidence.AndroidAudioCaptureStrategy
import com.numan.wcmp.core.evidence.AndroidCameraCaptureStrategy
import com.numan.wcmp.core.evidence.AndroidLocationCaptureStrategy
import com.numan.wcmp.core.monitoring.AccessibilityForegroundMonitor
import com.numan.wcmp.core.permissions.CapabilityManager
import com.numan.wcmp.core.permissions.CapabilityRepository
import com.numan.wcmp.core.permissions.DefaultRuntimePermissionManager
import com.numan.wcmp.core.permissions.DefaultSpecialAccessManager
import com.numan.wcmp.core.security.evidence.DefaultEvidenceKeyManager
import com.numan.wcmp.core.security.evidence.EncryptedEvidenceStorage
import com.numan.wcmp.data.local.apps.DefaultInstalledAppDataSource
import com.numan.wcmp.data.local.database.WcmpDatabaseProvider
import com.numan.wcmp.data.local.preferences.WcmpPreferences
import com.numan.wcmp.data.repository.DefaultProtectedAppsRepository
import com.numan.wcmp.data.repository.DefaultSecurityRepository
import com.numan.wcmp.data.repository.ProtectedAppsRepository
import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.DefaultEvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidencePolicy
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.domain.evidence.health.AndroidEvidenceDirectoryProvider
import com.numan.wcmp.domain.evidence.health.DefaultEvidenceIntegrityService
import com.numan.wcmp.domain.evidence.health.EvidenceHealthEvaluator
import com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService
import com.numan.wcmp.domain.protection.AppLockController
import com.numan.wcmp.domain.protection.DefaultAppLockPolicy
import com.numan.wcmp.domain.protection.DefaultAppLockPresenter
import com.numan.wcmp.domain.security.SecurityEngine
import com.numan.wcmp.domain.security.SecurityEventProcessor
import com.numan.wcmp.domain.security.SecuritySessionManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object WcmpRepositoryProvider {

    private val sharedSupervisorScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> }
    )

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
    private var evidenceIntegrityService: EvidenceIntegrityService? = null

    @Volatile
    private var securitySessionManager: SecuritySessionManager? = null

    fun getSecuritySessionManager(): SecuritySessionManager {
        return securitySessionManager ?: synchronized(this) {
            securitySessionManager ?: SecuritySessionManager().also {
                securitySessionManager = it
            }
        }
    }

    fun getEvidenceIntegrityService(context: Context): EvidenceIntegrityService {
        val appContext = context.applicationContext
        return evidenceIntegrityService ?: synchronized(this) {
            evidenceIntegrityService ?: run {
                val keyManager = DefaultEvidenceKeyManager()
                val directoryProvider = AndroidEvidenceDirectoryProvider(appContext)
                val healthEvaluator = EvidenceHealthEvaluator(appContext.filesDir, keyManager)
                DefaultEvidenceIntegrityService(directoryProvider, keyManager, healthEvaluator).also {
                    evidenceIntegrityService = it
                }
            }
        }
    }

    fun getEvidenceCaptureCoordinator(context: Context): EvidenceCaptureCoordinator {
        val appContext = context.applicationContext
        return evidenceCaptureCoordinator ?: synchronized(this) {
            evidenceCaptureCoordinator ?: run {
                val prefs = WcmpPreferences(appContext)
                val policy = EvidencePolicy(
                    cameraEnabledFlow = prefs.cameraEvidenceEnabled,
                    audioEnabledFlow = prefs.audioEvidenceEnabled,
                    locationEnabledFlow = prefs.locationEvidenceEnabled
                )
                val storage = EncryptedEvidenceStorage(
                    appContext.filesDir,
                    getSecurityRepository(appContext)
                )
                val strategies = mapOf(
                    EvidenceType.FRONT_CAMERA_PHOTO to AndroidCameraCaptureStrategy(appContext),
                    EvidenceType.AUDIO_CLIP to AndroidAudioCaptureStrategy(appContext),
                    EvidenceType.LOCATION_STAMP to AndroidLocationCaptureStrategy(appContext)
                )
                DefaultEvidenceCaptureCoordinator(policy, strategies, storage).also {
                    evidenceCaptureCoordinator = it
                }
            }
        }
    }

    fun getAppLockController(context: Context): AppLockController {
        val appContext = context.applicationContext
        return appLockController ?: synchronized(this) {
            appLockController ?: AppLockController(
                policy = DefaultAppLockPolicy(),
                securityRepository = getSecurityRepository(appContext),
                presenter = DefaultAppLockPresenter(appContext),
                evidenceCoordinator = getEvidenceCaptureCoordinator(appContext),
                coroutineScope = sharedSupervisorScope,
                sessionManager = getSecuritySessionManager()
            ).also {
                appLockController = it
            }
        }
    }

    fun getSecurityEngine(context: Context): SecurityEngine {
        val appContext = context.applicationContext
        return securityEngine ?: synchronized(this) {
            securityEngine ?: SecurityEngine(
                foregroundMonitor = AccessibilityForegroundMonitor,
                protectedAppsRepository = getProtectedAppsRepository(appContext),
                eventProcessor = SecurityEventProcessor(
                    getSecurityRepository(appContext),
                    getSecuritySessionManager()
                ),
                appLockController = getAppLockController(appContext),
                coroutineScope = sharedSupervisorScope,
                sessionManager = getSecuritySessionManager()
            ).also {
                securityEngine = it
            }
        }
    }

    fun setProtectedAppsRepositoryForTest(repo: ProtectedAppsRepository?) {
        synchronized(this) {
            protectedAppsRepository = repo
        }
    }

    fun getProtectedAppsRepository(context: Context): ProtectedAppsRepository {
        val appContext = context.applicationContext
        return protectedAppsRepository ?: synchronized(this) {
            protectedAppsRepository ?: run {
                val db = WcmpDatabaseProvider.getDatabase(appContext)
                val dataSource = DefaultInstalledAppDataSource(appContext)
                DefaultProtectedAppsRepository(
                    appDataSource = dataSource,
                    protectedAppDao = db.protectedAppDao()
                ).also {
                    protectedAppsRepository = it
                }
            }
        }
    }

    fun getSecurityRepository(context: Context): SecurityRepository {
        val appContext = context.applicationContext
        return securityRepository ?: synchronized(this) {
            securityRepository ?: run {
                val db = WcmpDatabaseProvider.getDatabase(appContext)
                val prefs = WcmpPreferences(appContext)
                DefaultSecurityRepository(
                    securityEventDao = db.securityEventDao(),
                    vaultEvidenceDao = db.vaultEvidenceDao(),
                    protectedAppDao = db.protectedAppDao(),
                    preferences = prefs
                ).also {
                    securityRepository = it
                }
            }
        }
    }

    fun getCapabilityRepository(context: Context): CapabilityRepository {
        val appContext = context.applicationContext
        return capabilityRepository ?: synchronized(this) {
            capabilityRepository ?: run {
                val runtimePermissionManager = DefaultRuntimePermissionManager(appContext)
                val specialAccessManager = DefaultSpecialAccessManager(appContext)
                val capabilityManager = CapabilityManager(
                    runtimePermissionManager,
                    specialAccessManager
                )
                CapabilityRepository(capabilityManager).also {
                    capabilityRepository = it
                }
            }
        }
    }
}
