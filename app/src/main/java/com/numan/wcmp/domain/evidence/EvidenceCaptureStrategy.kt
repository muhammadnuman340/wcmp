package com.numan.wcmp.domain.evidence

interface EvidenceCaptureStrategy {
    fun isCapabilityAvailable(): Boolean
    suspend fun capture(request: EvidenceCaptureRequest): EvidenceCaptureResult
}
