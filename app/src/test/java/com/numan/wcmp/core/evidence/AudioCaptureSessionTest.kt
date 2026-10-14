package com.numan.wcmp.core.evidence

import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class AudioCaptureSessionTest {

    @Before
    fun setup() {
        // Ensure clean state before each test
        AudioCaptureSession.complete(null)
    }

    @Test
    fun `test successful completion`() = runTest {
        AudioCaptureSession.prepareSession("MANUAL_TEST", "event123")
        
        val record = EvidenceRecord(
            id = UUID.randomUUID().toString(),
            type = EvidenceType.AUDIO_CLIP,
            localUri = "file://test",
            capturedAtTimestamp = 1234L,
            isEncrypted = false,
            fileSizeInBytes = 100L
        )

        val resultDeferred = async { AudioCaptureSession.awaitResult() }
        
        // Give the coroutine a moment to start
        delay(10)
        
        AudioCaptureSession.complete(record)
        
        val result = resultDeferred.await()
        assertEquals(record, result)
        assertNull(AudioCaptureSession.currentRequestTrigger)
        assertNull(AudioCaptureSession.currentRequestEventId)
    }

    @Test
    fun `test cancellation completion`() = runTest {
        AudioCaptureSession.prepareSession("MANUAL_TEST", "event123")
        
        val resultDeferred = async { AudioCaptureSession.awaitResult() }
        
        delay(10)
        
        AudioCaptureSession.complete(null)
        
        val result = resultDeferred.await()
        assertNull(result)
        assertNull(AudioCaptureSession.currentRequestTrigger)
        assertNull(AudioCaptureSession.currentRequestEventId)
    }

    @Test
    fun `test cancellation unbinds session safely`() = runTest {
        AudioCaptureSession.prepareSession("MANUAL_TEST", "event123")
        
        val resultDeferred = async { AudioCaptureSession.awaitResult() }
        
        delay(10)
        
        resultDeferred.cancel()
        
        // Ensure properties are cleared after cancel
        delay(10)
        assertNull(AudioCaptureSession.currentRequestTrigger)
        assertNull(AudioCaptureSession.currentRequestEventId)
    }
}
