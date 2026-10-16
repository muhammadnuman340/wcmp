package com.numan.wcmp.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.numan.wcmp.domain.protection.AuthenticationResult
import com.numan.wcmp.domain.protection.WcmpAuthenticator
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume

class AndroidWcmpAuthenticator(
    private val activity: FragmentActivity,
    private val maxFailedAttemptsBeforeCapture: Int = 1,
    private val onFailedAttempt: ((attemptNumber: Int) -> Unit)? = null
) : WcmpAuthenticator {
    override suspend fun authenticate(): AuthenticationResult =
        suspendCancellableCoroutine { continuation ->
            val biometricManager = BiometricManager.from(activity)
            val authenticators =
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL

            val canAuthenticate = biometricManager.canAuthenticate(authenticators)
            if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
                continuation.resume(AuthenticationResult.UNAVAILABLE)
                return@suspendCancellableCoroutine
            }

            val failedAttempts = AtomicInteger(0)
            val executor = ContextCompat.getMainExecutor(activity)
            lateinit var biometricPrompt: BiometricPrompt

            biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        val result = when (errorCode) {
                            BiometricPrompt.ERROR_LOCKOUT,
                            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                                AuthenticationResult.FAILED
                            }
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                            BiometricPrompt.ERROR_CANCELED -> {
                                if (failedAttempts.get() > 0) {
                                    AuthenticationResult.FAILED
                                } else {
                                    AuthenticationResult.CANCELLED
                                }
                            }
                            else -> {
                                if (failedAttempts.get() > 0) {
                                    AuthenticationResult.FAILED
                                } else {
                                    AuthenticationResult.ERROR
                                }
                            }
                        }
                        if (continuation.isActive) {
                            continuation.resume(result)
                        }
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        if (continuation.isActive) {
                            continuation.resume(AuthenticationResult.SUCCESS)
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        val count = failedAttempts.incrementAndGet()
                        onFailedAttempt?.invoke(count)
                        if (count >= maxFailedAttemptsBeforeCapture) {
                            biometricPrompt.cancelAuthentication()
                            if (continuation.isActive) {
                                continuation.resume(AuthenticationResult.FAILED)
                            }
                        }
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("WCMP Protection")
                .setSubtitle("Authenticate to continue to this protected app.")
                .setAllowedAuthenticators(authenticators)
                .build()

            biometricPrompt.authenticate(promptInfo)

            continuation.invokeOnCancellation {
                biometricPrompt.cancelAuthentication()
            }
        }
}
