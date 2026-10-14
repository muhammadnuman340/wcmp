package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.feature.vault.model.VaultEvidence
import java.io.File

interface EvidenceDecryptionService {
    fun decryptForViewing(evidence: VaultEvidence): File
    fun cleanupCache()
}
