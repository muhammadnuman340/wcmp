package com.numan.wcmp.feature.vault.preview

import com.numan.wcmp.domain.evidence.health.ControlledDecryptionService
import com.numan.wcmp.domain.evidence.health.EvidenceDecryptionService
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel
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
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class EvidencePreviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var decryptionService: EvidenceDecryptionService
    private lateinit var viewModel: EvidencePreviewViewModel
    private lateinit var decoder: SecureImageDecoder
    private lateinit var integrityService: com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        decryptionService = mock(EvidenceDecryptionService::class.java)
        decoder = mock(SecureImageDecoder::class.java)
        integrityService = mock(com.numan.wcmp.domain.evidence.health.EvidenceIntegrityService::class.java)
        kotlinx.coroutines.runBlocking { whenever(integrityService.inspect(any())).thenReturn(EvidenceHealth.HEALTHY) }
        viewModel = EvidencePreviewViewModel(decoder, decryptionService, integrityService, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createEvidenceModel(
        type: VaultEvidenceType,
        health: EvidenceHealth,
        latitude: Double? = null
    ): VaultEvidenceUiModel {
        val evidence = VaultEvidence(
            id = "test_id",
            title = "Test Evidence",
            type = type,
            relatedEvent = "Test Event",
            timestamp = "now",
            size = "1MB",
            category = VaultCategory.OTHER,
            latitude = latitude,
            longitude = if (latitude != null) 45.0 else null,
            accuracyMeters = if (latitude != null) 10f else null
        )
        return VaultEvidenceUiModel(evidence, health)
    }

    @Test
    fun `loadEvidence with Location sets LocationReady state`() = runTest {
        val model = createEvidenceModel(VaultEvidenceType.LOCATION, EvidenceHealth.HEALTHY, latitude = 34.0)
        
        viewModel.loadEvidence(model)
        
        val state = viewModel.uiState.value
        assertTrue(state is EvidencePreviewState.LocationReady)
        val locationState = state as EvidencePreviewState.LocationReady
        assertEquals(34.0, locationState.latitude, 0.001)
    }

    @Test
    fun `loadEvidence with IntegrityError sets IntegrityError state`() = runTest {
        val model = createEvidenceModel(VaultEvidenceType.PHOTO, EvidenceHealth.INTEGRITY_ERROR)
        
        viewModel.loadEvidence(model)
        
        assertEquals(EvidencePreviewState.IntegrityError, viewModel.uiState.value)
    }

    @Test
    fun `loadEvidence with KeyUnavailable sets KeyUnavailable state`() = runTest {
        val model = createEvidenceModel(VaultEvidenceType.AUDIO, EvidenceHealth.KEY_UNAVAILABLE)
        
        viewModel.loadEvidence(model)
        
        assertEquals(EvidencePreviewState.KeyUnavailable, viewModel.uiState.value)
    }

    @Test
    fun `loadEvidence decrypts PHOTO and sets ImageReady`() = runTest {
        val model = createEvidenceModel(VaultEvidenceType.PHOTO, EvidenceHealth.HEALTHY)
        val mockFile = File("test_temp.jpg")
        val mockBitmap = FakePreviewImageData()
        whenever(decryptionService.decryptForViewing(any())).thenReturn(mockFile)
        whenever(decoder.decode(any(), any(), any())).thenReturn(mockBitmap)
        
        viewModel.loadEvidence(model)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertTrue(state is EvidencePreviewState.ImageReady)
        assertEquals(mockBitmap, (state as EvidencePreviewState.ImageReady).image)
    }

    @Test
    fun `cleanup calls service cleanup`() {
        viewModel.cleanup()
        verify(decryptionService, org.mockito.Mockito.atLeastOnce()).cleanupCache()
    }
}








