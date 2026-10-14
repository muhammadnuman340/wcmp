package com.numan.wcmp.domain.evidence

interface EvidenceCaptureCoordinator {
    suspend fun requestCapture(request: EvidenceCaptureRequest): EvidenceCaptureResult
}

class DefaultEvidenceCaptureCoordinator(
    private val policy: EvidencePolicy,
    private val strategies: Map<EvidenceType, EvidenceCaptureStrategy>,
    private val storage: EvidenceStorage
) : EvidenceCaptureCoordinator {
    override suspend fun requestCapture(request: EvidenceCaptureRequest): EvidenceCaptureResult {
        if (!policy.isCaptureAllowed(request)) {
            return EvidenceCaptureResult.RejectedByPolicy
        }

        val strategy = strategies[request.preferredType]
            ?: return EvidenceCaptureResult.Failure("Unsupported evidence type: ${request.preferredType}")

        if (!strategy.isCapabilityAvailable()) {
            return EvidenceCaptureResult.CapabilityUnavailable
        }

        val result = strategy.capture(request)
        if (result is EvidenceCaptureResult.Success) {
            val title = "Captured ${request.preferredType.name}"
            val category = request.trigger.name
            storage.saveEvidenceRecord(result.evidenceRecord, request.relatedEventId, title, category, request.sessionId)
        }

        return result
    }
}
