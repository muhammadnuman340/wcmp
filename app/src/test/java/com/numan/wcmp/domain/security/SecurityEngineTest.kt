package com.numan.wcmp.domain.security

import com.numan.wcmp.data.local.apps.InstalledAppDataSource
import com.numan.wcmp.data.local.database.dao.ProtectedAppDao
import com.numan.wcmp.data.local.database.entity.ProtectedAppEntity
import com.numan.wcmp.data.repository.ProtectedAppsRepository
import com.numan.wcmp.feature.protectedapps.model.ProtectedApp
import com.numan.wcmp.feature.protectedapps.model.ProtectedAppCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class SecurityEngineTest {

    private lateinit var monitor: FakeForegroundMonitor
    private lateinit var engine: SecurityEngine
    private lateinit var eventProcessor: FakeSecurityEventProcessor
    private lateinit var fakeProtectedAppsRepository: FakeProtectedAppsRepository

    class FakeForegroundMonitor : ForegroundMonitor {
        val _status = MutableStateFlow(ForegroundMonitorStatus.STOPPED)
        override val status: Flow<ForegroundMonitorStatus> = _status

        val _foregroundApp = MutableStateFlow<ForegroundApp?>(null)
        override val foregroundApp: Flow<ForegroundApp?> = _foregroundApp

        var isStarted = false

        override fun start() {
            isStarted = true
            _status.value = ForegroundMonitorStatus.RUNNING
        }

        override fun stop() {
            isStarted = false
            _status.value = ForegroundMonitorStatus.STOPPED
        }
        
        fun emitApp(packageName: String) {
            _foregroundApp.value = ForegroundApp(packageName = packageName)
        }
    }

    class FakeProtectedAppsRepository : ProtectedAppsRepository {
        val apps = mutableListOf<ProtectedApp>()
        
        override fun observeProtectedAppsOnly(): Flow<List<ProtectedApp>> {
            return flowOf(apps)
        }
        
        override fun observeInstalledApps(): Flow<List<ProtectedApp>> {
            return flowOf(apps)
        }
        
        override suspend fun refreshInstalledApps() {}
        
        override suspend fun setProtected(packageName: String, isProtected: Boolean) {}
        
        override suspend fun setProtected(packageNames: List<String>, isProtected: Boolean) {}
    }

    class FakeSecurityEventProcessor : SecurityEventProcessor(null) {
        val events = mutableListOf<ForegroundApp>()
        
        override suspend fun processProtectedAppAccess(app: ForegroundApp) {
            events.add(app)
        }
    }

    @Before
    fun setup() {
        monitor = FakeForegroundMonitor()
        fakeProtectedAppsRepository = FakeProtectedAppsRepository()
        
        eventProcessor = FakeSecurityEventProcessor()
        
        engine = SecurityEngine(
            foregroundMonitor = monitor,
            protectedAppsRepository = fakeProtectedAppsRepository,
            eventProcessor = eventProcessor,
            appLockController = null,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `unprotected app foreground transition ignores event`() = runBlocking {
        engine.start()
        
        monitor.emitApp("com.unprotected.app")
        
        assertEquals(0, eventProcessor.events.size)
    }

    @Test
    fun `protected app foreground transition triggers access event`() = runBlocking {
        fakeProtectedAppsRepository.apps.add(
            ProtectedApp(
                packageName = "com.protected.app",
                name = "Protected",
                isProtected = true,
                category = ProtectedAppCategory.OTHER
            )
        )
        engine.start()
        
        monitor.emitApp("com.protected.app")
        
        assertEquals(1, eventProcessor.events.size)
        assertEquals("com.protected.app", eventProcessor.events[0].packageName)
    }

    @Test
    fun `duplicate foreground updates in same session trigger only one event`() = runBlocking {
        fakeProtectedAppsRepository.apps.add(
            ProtectedApp(
                packageName = "com.protected.app",
                name = "Protected",
                isProtected = true,
                category = ProtectedAppCategory.OTHER
            )
        )
        engine.start()
        
        // First access
        monitor.emitApp("com.protected.app")
        // Duplicate update (e.g. Activity transition inside same app)
        monitor.emitApp("com.protected.app")
        monitor.emitApp("com.protected.app")
        
        assertEquals(1, eventProcessor.events.size)
    }

    @Test
    fun `leaving and returning triggers two distinct events`() = runBlocking {
        fakeProtectedAppsRepository.apps.add(
            ProtectedApp(
                packageName = "com.protected.app",
                name = "Protected",
                isProtected = true,
                category = ProtectedAppCategory.OTHER
            )
        )
        engine.start()
        
        // First access
        monitor.emitApp("com.protected.app")
        
        // Leave app (e.g., go to launcher)
        monitor.emitApp("com.android.launcher")
        
        // Return to app
        monitor.emitApp("com.protected.app")
        
        assertEquals(2, eventProcessor.events.size)
    }

    @Test
    fun `multiple different protected apps trigger respective events`() = runBlocking {
        fakeProtectedAppsRepository.apps.add(ProtectedApp(packageName = "com.protected.a", name = "A", isProtected = true, category = ProtectedAppCategory.OTHER))
        fakeProtectedAppsRepository.apps.add(ProtectedApp(packageName = "com.protected.b", name = "B", isProtected = true, category = ProtectedAppCategory.OTHER))
        
        engine.start()
        
        monitor.emitApp("com.protected.a")
        monitor.emitApp("com.protected.b")
        
        assertEquals(2, eventProcessor.events.size)
        assertEquals("com.protected.a", eventProcessor.events[0].packageName)
        assertEquals("com.protected.b", eventProcessor.events[1].packageName)
    }

    @Test
    fun `WCMP itself is ignored`() = runBlocking {
        fakeProtectedAppsRepository.apps.add(ProtectedApp(packageName = "com.numan.wcmp", name = "WCMP", isProtected = true, category = ProtectedAppCategory.OTHER))
        engine.start()
        
        monitor.emitApp("com.numan.wcmp")
        
        assertEquals(0, eventProcessor.events.size)
    }
}
