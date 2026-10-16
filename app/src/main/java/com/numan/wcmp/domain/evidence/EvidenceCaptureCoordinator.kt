package com.numan.wcmp.domain.evidence

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface EvidenceCaptureCoordinator {
    suspend fun requestCapture(request: EvidenceCaptureRequest): EvidenceCaptureResult
}

class DefaultEvidenceCaptureCoordinator(
    private val policy: EvidencePolicy,
    private val strategies: Map<EvidenceType, EvidenceCaptureStrategy>,
    private val storage: EvidenceStorage
) : EvidenceCaptureCoordinator {

    private val captureMutex = Mutex()

    override suspend fun requestCapture(
        request: EvidenceCaptureRequest
    ): EvidenceCaptureResult = captureMutex.withLock {
        if (!policy.isCaptureAllowed(request)) {
            return@withLock EvidenceCaptureResult.RejectedByPolicy
        }

        val strategy = strategies[request.preferredType]
            ?: return@withLock EvidenceCaptureResult.Failure(
                "Unsupported evidence type: ${request.preferredType}"
            )

        if (!strategy.isCapabilityAvailable()) {
            return@withLock EvidenceCaptureResult.CapabilityUnavailable
        }

        val result = try {
            strategy.capture(request)
        } catch (e: Exception) {
            return@withLock EvidenceCaptureResult.Failure(
                e.message ?: "Hardware capture failed"
            )
        }

        if (result is EvidenceCaptureResult.Success) {
            val title = when (request.preferredType) {
                EvidenceType.FRONT_CAMERA_PHOTO -> "Front Camera Photo"
                EvidenceType.AUDIO_CLIP -> "Ambient Audio Clip"
                EvidenceType.LOCATION_STAMP -> "Incident Location Stamp"
                EvidenceType.AUDIT_LOG_SNAPSHOT -> "Audit Log Snapshot"
            }
            val category = request.trigger.name

            try {
                storage.saveEvidenceRecord(
                    record = result.evidenceRecord,
                    relatedEventId = request.relatedEventId,
                    title = title,
                    category = category,
                    sessionId = request.sessionId
                )
            } catch (e: Exception) {
                return@withLock EvidenceCaptureResult.Failure(
                    "Failed to encrypt and store evidence: ${e.message}"
                )
            }
        }

        return@withLock result
    }
}
