package com.numan.wcmp.feature.settings.diagnostics

import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.domain.evidence.EvidenceCaptureCoordinator
import com.numan.wcmp.domain.evidence.EvidenceCaptureResult
import com.numan.wcmp.domain.evidence.EvidenceRecord
import com.numan.wcmp.domain.evidence.EvidenceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class EvidenceDiagnosticsViewModelTest {

    private lateinit var viewModel: EvidenceDiagnosticsViewModel
    private val coordinator = mock<EvidenceCaptureCoordinator>()
    private val repository = mock<SecurityRepository>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EvidenceDiagnosticsViewModel(coordinator, repository)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.testState.value is EvidenceTestState.Idle)
    }

    @Test
    fun `runPipelineTest creates success state when coordinator succeeds`() = runTest {
        val mockRecord = EvidenceRecord(
            id = "test-id",
            type = EvidenceType.FRONT_CAMERA_PHOTO,
            capturedAtTimestamp = 1234L,
            fileSizeInBytes = 0L,
            localUri = "test-path"
        )
        whenever(coordinator.requestCapture(any())).thenReturn(EvidenceCaptureResult.Success(mockRecord))

        viewModel.runPipelineTest(EvidenceType.FRONT_CAMERA_PHOTO)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.testState.value
        assertTrue(state is EvidenceTestState.Success)
        assertEquals("test-id", (state as EvidenceTestState.Success).evidenceId)
    }

    @Test
    fun `runPipelineTest creates blocked state when policy rejects`() = runTest {
        whenever(coordinator.requestCapture(any())).thenReturn(EvidenceCaptureResult.RejectedByPolicy)

        viewModel.runPipelineTest(EvidenceType.FRONT_CAMERA_PHOTO)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.testState.value is EvidenceTestState.Blocked)
    }

    @Test
    fun `runPipelineTest creates failed state when capability unavailable`() = runTest {
        whenever(coordinator.requestCapture(any())).thenReturn(EvidenceCaptureResult.CapabilityUnavailable)

        viewModel.runPipelineTest(EvidenceType.FRONT_CAMERA_PHOTO)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.testState.value is EvidenceTestState.Failed)
    }

    @Test
    fun `clearTestEvidence calls repository deleteTestEvidence`() = runTest {
        viewModel.clearTestEvidence()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).deleteTestEvidence()
    }

    @Test
    fun `runIntegrityCheck updates healthMetrics when inspection completes`() = runTest {
        val integrityService = mock<com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService>()
        whenever(repository.observeVaultEvidence()).thenReturn(kotlinx.coroutines.flow.flowOf(emptyList()))
        whenever(integrityService.scanForOrphans(any())).thenReturn(emptyList())

        viewModel.runIntegrityCheck(integrityService)
        testDispatcher.scheduler.advanceUntilIdle()

        val metrics = viewModel.healthMetrics.value
        assertTrue(metrics.hasRunCheck)
        assertEquals(0, metrics.healthy)
        assertEquals(0, metrics.missing)
    }
}
