package com.numan.wcmp.domain.evidence.health

import com.numan.wcmp.feature.vault.model.VaultEvidence
import java.time.Instant

interface EvidenceRetentionPolicy {
    fun shouldRetain(
        evidence: VaultEvidence,
        now: Instant
    ): Boolean
}

class DefaultEvidenceRetentionPolicy : EvidenceRetentionPolicy {
    override fun shouldRetain(evidence: VaultEvidence, now: Instant): Boolean {
        // No automatic deletion by default. 
        // Any automatic retention deletion must require an explicitly enabled configuration.
        return true
    }
}
