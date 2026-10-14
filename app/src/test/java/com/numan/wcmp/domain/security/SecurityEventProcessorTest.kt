package com.numan.wcmp.domain.security

import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.data.repository.SecurityRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class SecurityEventProcessorTest {

    @Test
    fun `processProtectedAppAccess creates HIGH severity event and correlates to session`() = runTest {
        val repository: SecurityRepository = mock()
        val sessionManager = SecuritySessionManager()
        
        // Start a session
        val sessionId = sessionManager.startSession("Test Trigger", "com.test.app")
        
        val processor = SecurityEventProcessor(repository, sessionManager)
        
        val app = ForegroundApp(
            packageName = "com.test.app",
            applicationName = "Test App",
            observedAt = 123456789L
        )
        
        processor.processProtectedAppAccess(app)
        
        val captor = argumentCaptor<SecurityEventEntity>()
        verify(repository).insertSecurityEvent(captor.capture())
        
        val event = captor.firstValue
        assertEquals("HIGH", event.severity)
        assertEquals("PROTECTED_APP_ACCESS", event.type)
        assertEquals("com.test.app", event.packageName)
        assertEquals("Test App", event.applicationLabel)
        assertEquals(sessionId, event.sessionId)
        assertEquals("RECORDED", event.status)
        assertEquals(1, sessionManager.currentSession.value?.eventCount)
    }

    @Test
    fun `processProtectedAppAccess without session handles null gracefully`() = runTest {
        val repository: SecurityRepository = mock()
        val sessionManager = SecuritySessionManager()
        // No session started
        
        val processor = SecurityEventProcessor(repository, sessionManager)
        
        val app = ForegroundApp(
            packageName = "com.test.app",
            applicationName = "Test App",
            observedAt = 123456789L
        )
        
        processor.processProtectedAppAccess(app)
        
        val captor = argumentCaptor<SecurityEventEntity>()
        verify(repository).insertSecurityEvent(captor.capture())
        
        val event = captor.firstValue
        assertNull(event.sessionId)
        assertEquals("HIGH", event.severity)
    }
}
