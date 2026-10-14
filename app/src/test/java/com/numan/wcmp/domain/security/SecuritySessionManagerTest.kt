package com.numan.wcmp.domain.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SecuritySessionManagerTest {

    @Test
    fun `session lifecycle tracks counts correctly`() {
        val manager = SecuritySessionManager()
        
        assertNull(manager.currentSession.value)
        
        val sessionId = manager.startSession("Trigger", "com.test")
        assertNotNull(manager.currentSession.value)
        assertEquals(sessionId, manager.currentSession.value?.sessionId)
        assertEquals(0, manager.currentSession.value?.eventCount)
        assertEquals(0, manager.currentSession.value?.evidenceCount)
        
        manager.recordEvent()
        manager.recordEvent()
        assertEquals(2, manager.currentSession.value?.eventCount)
        
        manager.recordEvidence()
        assertEquals(1, manager.currentSession.value?.evidenceCount)
        
        manager.endSession(SecuritySessionStatus.COMPLETED)
        assertNull(manager.currentSession.value)
    }
}
