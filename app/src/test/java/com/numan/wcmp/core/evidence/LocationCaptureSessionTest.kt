package com.numan.wcmp.core.evidence

import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class LocationCaptureSessionTest {

    @Before
    fun setup() {
        LocationCaptureSession.complete(null)
    }

    @Test
    fun `prepareSession sets active triggers`() {
        LocationCaptureSession.prepareSession("TEST_TRIGGER", "event_123")
        assertEquals("TEST_TRIGGER", LocationCaptureSession.currentRequestTrigger)
        assertEquals("event_123", LocationCaptureSession.currentRequestEventId)
    }

    @Test
    fun `awaitResult returns evidence when completed successfully`() = runTest {
        val record = EvidenceRecord(
            id = UUID.randomUUID().toString(),
            type = EvidenceType.LOCATION_STAMP,
            localUri = "wcmp://location",
            capturedAtTimestamp = 1000L
        )

        val deferred = async {
            LocationCaptureSession.awaitResult()
        }

        kotlinx.coroutines.yield()
        LocationCaptureSession.complete(record)

        val result = deferred.await()
        assertEquals(record, result)
    }

    @Test
    fun `awaitResult returns null when completed with null`() = runTest {
        val deferred = async {
            LocationCaptureSession.awaitResult()
        }

        kotlinx.coroutines.yield()
        LocationCaptureSession.complete(null)

        val result = deferred.await()
        assertNull(result)
    }

    @Test
    fun `awaitResult clears state on cancellation`() = runTest {
        LocationCaptureSession.prepareSession("TRIGGER", "EVENT")

        val deferred = async {
            LocationCaptureSession.awaitResult()
        }

        // yield to let the coroutine suspend
        delay(10)
        
        deferred.cancelAndJoin()

        assertNull(LocationCaptureSession.currentRequestTrigger)
        assertNull(LocationCaptureSession.currentRequestEventId)
    }
}
