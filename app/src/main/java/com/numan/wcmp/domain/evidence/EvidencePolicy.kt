package com.numan.wcmp.domain.evidence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class EvidencePolicy(
    private val cameraEnabledFlow: Flow<Boolean>,
    private val audioEnabledFlow: Flow<Boolean>,
    private val locationEnabledFlow: Flow<Boolean>
) {
    suspend fun isCaptureAllowed(request: EvidenceCaptureRequest): Boolean {
        return when (request.trigger) {
            EvidenceTrigger.MANUAL_TEST,
            EvidenceTrigger.MANUAL_CAPTURE -> true

            EvidenceTrigger.AUTHENTICATION_FAILED,
            EvidenceTrigger.AUTHENTICATION_CANCELLED -> {
                when (request.preferredType) {
                    EvidenceType.FRONT_CAMERA_PHOTO -> cameraEnabledFlow.first()
                    EvidenceType.AUDIO_CLIP -> audioEnabledFlow.first()
                    EvidenceType.LOCATION_STAMP -> locationEnabledFlow.first()
                    else -> false
                }
            }

            EvidenceTrigger.PROTECTED_APP_ACCESS -> false
        }
    }
}
