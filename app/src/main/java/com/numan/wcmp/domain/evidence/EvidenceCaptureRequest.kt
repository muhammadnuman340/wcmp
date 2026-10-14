package com.numan.wcmp.domain.evidence

data class EvidenceCaptureRequest(
    val trigger: EvidenceTrigger,
    val preferredType: EvidenceType,
    val relatedEventId: String? = null,
    val sessionId: String? = null
)
