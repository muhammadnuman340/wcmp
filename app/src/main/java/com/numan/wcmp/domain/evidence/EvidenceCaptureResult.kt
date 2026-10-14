package com.numan.wcmp.domain.evidence

sealed class EvidenceCaptureResult {
    data class Success(val evidenceRecord: EvidenceRecord) : EvidenceCaptureResult()
    data class Failure(val reason: String) : EvidenceCaptureResult()
    object RejectedByPolicy : EvidenceCaptureResult()
    object CapabilityUnavailable : EvidenceCaptureResult()
}
