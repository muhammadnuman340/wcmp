package com.numan.wcmp.core.evidence

import com.numan.wcmp.domain.evidence.EvidenceRecord
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object CameraCaptureSession {
    private val lock = Any()
    private var activeContinuation: CancellableContinuation<EvidenceRecord?>? = null

    @Volatile
    var currentRequestTrigger: String? = null
        private set

    @Volatile
    var currentRequestEventId: String? = null
        private set

    fun prepareSession(trigger: String, relatedEventId: String?) {
        synchronized(lock) {
            currentRequestTrigger = trigger
            currentRequestEventId = relatedEventId
        }
    }

    suspend fun awaitResult(): EvidenceRecord? =
        suspendCancellableCoroutine { cont ->
            synchronized(lock) {
                // Cancel any stale continuation so it never leaks
                activeContinuation?.cancel()
                activeContinuation = cont
            }

            cont.invokeOnCancellation {
                synchronized(lock) {
                    if (activeContinuation === cont) {
                        activeContinuation = null
                        currentRequestTrigger = null
                        currentRequestEventId = null
                    }
                }
            }
        }

    /**
     * Completes the active session and returns true if a waiting caller received the record.
     */
    fun complete(record: EvidenceRecord?): Boolean {
        val cont = synchronized(lock) {
            val current = activeContinuation
            activeContinuation = null
            currentRequestTrigger = null
            currentRequestEventId = null
            current
        }

        return if (cont != null && cont.isActive) {
            cont.resume(record)
            true
        } else {
            false
        }
    }
}
