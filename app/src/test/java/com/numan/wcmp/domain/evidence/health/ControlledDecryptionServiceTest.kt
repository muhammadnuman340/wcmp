package com.numan.wcmp.domain.evidence.health


import com.numan.wcmp.core.security.evidence.EvidenceKeyManager
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.io.File

class ControlledDecryptionServiceTest {

    private lateinit var directoryProvider: EvidenceDirectoryProvider
    private lateinit var keyManager: EvidenceKeyManager
    private lateinit var service: ControlledDecryptionService
    private lateinit var tempDir: File

    @Before
    fun setup() {
        
        keyManager = mock(EvidenceKeyManager::class.java)
        
        // Setup temporary directories for testing
        val systemTemp = System.getProperty("java.io.tmpdir")
        tempDir = File(systemTemp, "wcmp_test_context")
        tempDir.mkdirs()
        
                directoryProvider = object : EvidenceDirectoryProvider {
            override fun getEvidenceDirectory(): File = File(tempDir, "evidence").apply { mkdirs() }
            override fun getControlledViewingDirectory(): File = File(tempDir, "controlled_viewing").apply { mkdirs() }
        }

        service = ControlledDecryptionService(directoryProvider, keyManager)
    }

    @Test
    fun `decryptForViewing throws exception if file is missing`() {
        val evidence = VaultEvidence(
            id = "missing_id",
            title = "Missing",
            type = VaultEvidenceType.PHOTO,
            relatedEvent = "Event",
            timestamp = "now",
            size = "0",
            category = VaultCategory.OTHER
        )

        assertThrows(IllegalStateException::class.java) {
            service.decryptForViewing(evidence)
        }
    }

    @Test
    fun `cleanupCache deletes files in controlled viewing dir`() {
        val viewingDir = directoryProvider.getControlledViewingDirectory()
        viewingDir.mkdirs()
        val dummyFile = File(viewingDir, "test.txt")
        dummyFile.writeText("test")
        
        assert(dummyFile.exists())
        
        service.cleanupCache()
        
        assert(!dummyFile.exists())
    }
}


