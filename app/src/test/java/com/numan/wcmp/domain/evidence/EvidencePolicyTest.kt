package com.numan.wcmp.domain.evidence

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidencePolicyTest {

    @Test
    fun `capture rejected if camera disabled`() = runBlocking {
        val policy = EvidencePolicy(flowOf(false), flowOf(true), flowOf(true))
        val request = EvidenceCaptureRequest(EvidenceTrigger.AUTHENTICATION_FAILED, EvidenceType.FRONT_CAMERA_PHOTO)
        assertFalse(policy.isCaptureAllowed(request))
    }

    @Test
    fun `capture allowed if camera enabled and trigger valid`() = runBlocking {
        val policy = EvidencePolicy(flowOf(true), flowOf(false), flowOf(false))
        val request = EvidenceCaptureRequest(EvidenceTrigger.AUTHENTICATION_FAILED, EvidenceType.FRONT_CAMERA_PHOTO)
        assertTrue(policy.isCaptureAllowed(request))
    }

    @Test
    fun `capture rejected for invalid trigger`() = runBlocking {
        val policy = EvidencePolicy(flowOf(true), flowOf(true), flowOf(true))
        val request = EvidenceCaptureRequest(EvidenceTrigger.PROTECTED_APP_ACCESS, EvidenceType.FRONT_CAMERA_PHOTO)
        assertFalse(policy.isCaptureAllowed(request))
    }
}
