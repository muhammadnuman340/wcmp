package com.numan.wcmp.domain.evidence

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class EvidencePolicy(
    private val cameraEnabledFlow: Flow<Boolean>,
    private val audioEnabledFlow: Flow<Boolean>,
    private val locationEnabledFlow: Flow<Boolean>
) {
    suspend fun isCaptureAllowed(request: EvidenceCaptureRequest): Boolean {
        // Only allow manual test and authentication failures to trigger evidence
        if (request.trigger != EvidenceTrigger.AUTHENTICATION_FAILED && 
            request.trigger != EvidenceTrigger.MANUAL_TEST) {
            return false
        }

        return when (request.preferredType) {
            EvidenceType.FRONT_CAMERA_PHOTO -> cameraEnabledFlow.first()
            EvidenceType.AUDIO_CLIP -> audioEnabledFlow.first()
            EvidenceType.LOCATION_STAMP -> locationEnabledFlow.first()
            else -> false // Unhandled or disabled types
        }
    }
}
