package com.numan.wcmp.core.evidence

import com.numan.wcmp.domain.evidence.EvidenceRecord
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

object CameraCaptureSession {
    private var activeContinuation: Continuation<EvidenceRecord?>? = null
    var currentRequestTrigger: String? = null
        private set
    var currentRequestEventId: String? = null
        private set

    fun prepareSession(trigger: String, relatedEventId: String?) {
        currentRequestTrigger = trigger
        currentRequestEventId = relatedEventId
    }

    suspend fun awaitResult(): EvidenceRecord? = suspendCancellableCoroutine { cont ->
        activeContinuation = cont
        cont.invokeOnCancellation {
            activeContinuation = null
            currentRequestTrigger = null
            currentRequestEventId = null
        }
    }

    fun complete(record: EvidenceRecord?): Boolean {
        val cont = activeContinuation
        activeContinuation = null
        currentRequestTrigger = null
        currentRequestEventId = null
        return if (cont is kotlinx.coroutines.CancellableContinuation) {
            if (cont.isActive) {
                cont.resume(record, null)
                true
            } else {
                false
            }
        } else if (cont != null) {
            cont.resume(record)
            true
        } else {
            false
        }
    }
}
