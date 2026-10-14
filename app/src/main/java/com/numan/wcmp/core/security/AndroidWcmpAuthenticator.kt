package com.numan.wcmp.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.numan.wcmp.domain.protection.AuthenticationResult
import com.numan.wcmp.domain.protection.WcmpAuthenticator
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidWcmpAuthenticator(
    private val activity: FragmentActivity
) : WcmpAuthenticator {

    override suspend fun authenticate(): AuthenticationResult = suspendCancellableCoroutine { continuation ->
        
        val biometricManager = BiometricManager.from(activity)
        // Check if biometric (or device credential) is available
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        
        val canAuthenticate = biometricManager.canAuthenticate(authenticators)
        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            continuation.resume(AuthenticationResult.UNAVAILABLE)
            return@suspendCancellableCoroutine
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    val result = if (errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        AuthenticationResult.CANCELLED
                    } else {
                        AuthenticationResult.ERROR
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
                    // Prompt stays open on failure for retry, we don't immediately resume FAILED
                    // unless we want to fail the whole process.
                    // BiometricPrompt handles retries internally. We only resume on terminal states.
                }
            })

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
