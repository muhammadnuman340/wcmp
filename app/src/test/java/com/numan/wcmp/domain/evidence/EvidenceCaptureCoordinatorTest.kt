package com.numan.wcmp.domain.evidence

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EvidenceCaptureCoordinatorTest {

    class FakeStrategy(private val available: Boolean, private val success: Boolean = true) : EvidenceCaptureStrategy {
        override fun isCapabilityAvailable() = available

        override suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
            if (!available) return EvidenceCaptureResult.CapabilityUnavailable
            return if (success) {
                EvidenceCaptureResult.Success(EvidenceRecord("test_id", request.preferredType, "file://test", 0L))
            } else {
                EvidenceCaptureResult.Failure("Test Failure")
            }
        }
    }

    class FakeStorage : EvidenceStorage {
        var lastSavedRecord: EvidenceRecord? = null
        override suspend fun saveEvidenceRecord(record: EvidenceRecord, relatedEventId: String?, title: String, category: String, sessionId: String?) {
            lastSavedRecord = record
        }
        override suspend fun deleteEvidenceRecord(id: String) {}
    }

    @Test
    fun `capture rejected by policy`() = runBlocking {
        val policy = EvidencePolicy(flowOf(false), flowOf(false), flowOf(false))
        val coordinator = DefaultEvidenceCaptureCoordinator(policy, emptyMap(), FakeStorage())
        
        val result = coordinator.requestCapture(EvidenceCaptureRequest(EvidenceTrigger.AUTHENTICATION_FAILED, EvidenceType.FRONT_CAMERA_PHOTO))
        assertEquals(EvidenceCaptureResult.RejectedByPolicy, result)
    }

    @Test
    fun `capture fails if capability unavailable`() = runBlocking {
        val policy = EvidencePolicy(flowOf(true), flowOf(false), flowOf(false))
        val strategies = mapOf(EvidenceType.FRONT_CAMERA_PHOTO to FakeStrategy(available = false))
        val coordinator = DefaultEvidenceCaptureCoordinator(policy, strategies, FakeStorage())
        
        val result = coordinator.requestCapture(EvidenceCaptureRequest(EvidenceTrigger.AUTHENTICATION_FAILED, EvidenceType.FRONT_CAMERA_PHOTO))
        assertEquals(EvidenceCaptureResult.CapabilityUnavailable, result)
    }

    @Test
    fun `successful capture saves evidence`() = runBlocking {
        val policy = EvidencePolicy(flowOf(true), flowOf(false), flowOf(false))
        val strategies = mapOf(EvidenceType.FRONT_CAMERA_PHOTO to FakeStrategy(available = true))
        val storage = FakeStorage()
        val coordinator = DefaultEvidenceCaptureCoordinator(policy, strategies, storage)
        
        val result = coordinator.requestCapture(EvidenceCaptureRequest(EvidenceTrigger.AUTHENTICATION_FAILED, EvidenceType.FRONT_CAMERA_PHOTO))
        assertTrue(result is EvidenceCaptureResult.Success)
        assertEquals("test_id", storage.lastSavedRecord?.id)
    }
}
