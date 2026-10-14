package com.numan.wcmp.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class CapabilityManagerTest {

    private val runtimePermissionManager = mock<RuntimePermissionManager> {
        on { checkPermissionStatus(any()) } doReturn CapabilityStatus.DENIED
        on { checkPermissionStatus(AndroidCapability.CAMERA) } doReturn CapabilityStatus.GRANTED
        on { checkPermissionStatus(AndroidCapability.PACKAGE_VISIBILITY) } doReturn CapabilityStatus.NOT_IMPLEMENTED
        on { checkPermissionStatus(AndroidCapability.USAGE_ACCESS) } doReturn CapabilityStatus.DENIED
        on { canRequest(any()) } doReturn false
    }

    private val specialAccessManager = mock<SpecialAccessManager> {
        on { checkSpecialAccessStatus(any()) } doReturn CapabilityStatus.DENIED
        on { checkSpecialAccessStatus(AndroidCapability.ACCESSIBILITY_SERVICE) } doReturn CapabilityStatus.REQUIRES_SPECIAL_ACCESS
        on { canOpenSettings(any()) } doReturn false
        on { canOpenSettings(AndroidCapability.ACCESSIBILITY_SERVICE) } doReturn true
    }

    @Test
    fun `when capability is not implemented, state is NOT_IMPLEMENTED`() {
        val manager = CapabilityManager(runtimePermissionManager, specialAccessManager)

        val state = manager.getCapabilityState(AndroidCapability.FOREGROUND_SERVICE)
        
        assertEquals(CapabilityStatus.NOT_IMPLEMENTED, state.status)
        assertFalse(state.canRequest)
    }

    @Test
    fun `when capability is runtime permission, it delegates to RuntimePermissionManager`() {
        val manager = CapabilityManager(runtimePermissionManager, specialAccessManager)

        val state = manager.getCapabilityState(AndroidCapability.CAMERA)
        
        assertEquals(CapabilityStatus.GRANTED, state.status)
        assertFalse(state.canRequest)
    }

    @Test
    fun `all capability states map every enum entry`() {
        val manager = CapabilityManager(runtimePermissionManager, specialAccessManager)

        val states = manager.getAllCapabilityStates()
        
        assertEquals(AndroidCapability.entries.size, states.size)
        assertTrue(states.any { it.capability == AndroidCapability.CAMERA })
        assertTrue(states.any { it.capability == AndroidCapability.USAGE_ACCESS })
    }
}
