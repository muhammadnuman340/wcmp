package com.numan.wcmp.domain.protection

import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.DefaultEvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidencePolicy
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceStorage
import com.numan.wcmp.domain.security.ForegroundApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class AppLockControllerTest {

    private lateinit var controller: AppLockController
    private lateinit var presenter: FakeAppLockPresenter
    private lateinit var mockSecurityRepository: SecurityRepository
    private lateinit var dummyCoordinator: EvidenceCaptureCoordinator

    class FakeAppLockPresenter : AppLockPresenter {
        var lastPresentedPackage: String? = null
        override fun presentLockScreen(packageName: String, applicationName: String?) {
            lastPresentedPackage = packageName
        }
    }
    
    class DummyStorage : EvidenceStorage {
        override suspend fun saveEvidenceRecord(record: EvidenceRecord, relatedEventId: String?, title: String, category: String, sessionId: String?) {}
        override suspend fun deleteEvidenceRecord(id: String) {}
    }

    @Before
    fun setup() {
        presenter = FakeAppLockPresenter()
        mockSecurityRepository = mock {
            on { autoProtectionEnabled } doReturn flowOf(true)
        }
        
        dummyCoordinator = DefaultEvidenceCaptureCoordinator(
            policy = EvidencePolicy(flowOf(false), flowOf(false), flowOf(false)),
            strategies = emptyMap(),
            storage = DummyStorage()
        )
        
        controller = AppLockController(
            policy = DefaultAppLockPolicy(),
            securityRepository = mockSecurityRepository,
            presenter = presenter,
            evidenceCoordinator = dummyCoordinator,
            coroutineScope = CoroutineScope(Dispatchers.Unconfined)
        )
    }

    @Test
    fun `unprotected package is ignored`() = runBlocking {
        val decision = DefaultAppLockPolicy().requiresAuthentication("com.unprotected", false, true, null)
        assertEquals(AppLockDecision.Ignore, decision)
    }

    @Test
    fun `protected package with locking disabled is allowed`() = runBlocking {
        val localMockRepo = mock<SecurityRepository> {
            on { autoProtectionEnabled } doReturn flowOf(false)
        }
        
        val localController = AppLockController(
            DefaultAppLockPolicy(), localMockRepo, presenter, dummyCoordinator, CoroutineScope(Dispatchers.Unconfined)
        )
        
        localController.onProtectedAppAccessed(ForegroundApp("com.protected"))
        assertEquals(AppLockState.UNLOCKED, localController.state.value)
        assertEquals(null, presenter.lastPresentedPackage)
    }

    @Test
    fun `protected package with locking enabled and no session requires authentication`() = runBlocking {
        controller.onProtectedAppAccessed(ForegroundApp("com.protected"))
        
        assertEquals(AppLockState.LOCK_REQUIRED, controller.state.value)
        assertEquals("com.protected", presenter.lastPresentedPackage)
    }

    @Test
    fun `authentication success creates session and allows access`() = runBlocking {
        controller.onProtectedAppAccessed(ForegroundApp("com.protected"))
        assertEquals(AppLockState.LOCK_REQUIRED, controller.state.value)
        
        controller.onAuthenticationCompleted("com.protected", AuthenticationResult.SUCCESS)
        
        assertEquals(AppLockState.UNLOCKED, controller.state.value)
        
        presenter.lastPresentedPackage = null
        controller.onProtectedAppAccessed(ForegroundApp("com.protected"))
        
        assertEquals(AppLockState.UNLOCKED, controller.state.value)
        assertEquals(null, presenter.lastPresentedPackage)
    }

    @Test
    fun `authentication failure denies access`() = runBlocking {
        controller.onProtectedAppAccessed(ForegroundApp("com.protected"))
        controller.onAuthenticationCompleted("com.protected", AuthenticationResult.FAILED)
        
        assertEquals(AppLockState.DENIED, controller.state.value)
    }

    @Test
    fun `authentication cancellation denies access`() = runBlocking {
        controller.onProtectedAppAccessed(ForegroundApp("com.protected"))
        controller.onAuthenticationCompleted("com.protected", AuthenticationResult.CANCELLED)
        
        assertEquals(AppLockState.DENIED, controller.state.value)
    }

    @Test
    fun `leaving protected application clears session`() = runBlocking {
        controller.onAuthenticationCompleted("com.protected", AuthenticationResult.SUCCESS)
        assertEquals(AppLockState.UNLOCKED, controller.state.value)
        
        controller.onProtectedAppLeft("com.protected")
        assertEquals(AppLockState.WAITING_FOR_PROTECTED_APP, controller.state.value)
        
        controller.onProtectedAppAccessed(ForegroundApp("com.protected"))
        assertEquals(AppLockState.LOCK_REQUIRED, controller.state.value)
    }

    @Test
    fun `multiple protected packages maintain isolation`() = runBlocking {
        controller.onAuthenticationCompleted("com.app.a", AuthenticationResult.SUCCESS)
        
        controller.onProtectedAppAccessed(ForegroundApp("com.app.b"))
        assertEquals(AppLockState.LOCK_REQUIRED, controller.state.value)
        assertEquals("com.app.b", presenter.lastPresentedPackage)
    }
}
