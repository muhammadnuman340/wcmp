package com.numan.wcmp.feature.applock

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.biometric.BiometricManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.numan.wcmp.core.security.AndroidWcmpAuthenticator
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.protection.AuthenticationResult
import com.numan.wcmp.ui.theme.WCMPTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AppLockActivity : FragmentActivity() {

    private val viewModel: AppLockViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appLockController =
                    WcmpRepositoryProvider.getAppLockController(applicationContext)
                return AppLockViewModel(appLockController) as T
            }
        }
    }

    private var authJob: Job? = null
    private var isAuthenticated = false
    private var uiStatusState by mutableStateOf("READY")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        val packageName = intent.getStringExtra("EXTRA_PACKAGE_NAME") ?: return finish()
        val applicationName = intent.getStringExtra("EXTRA_APPLICATION_NAME")

        val authenticator = AndroidWcmpAuthenticator(this)
        val biometricManager = BiometricManager.from(this)
        val authenticators =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val isBiometricAvailable =
            biometricManager.canAuthenticate(authenticators) ==
                BiometricManager.BIOMETRIC_SUCCESS

        uiStatusState = if (isBiometricAvailable) "READY" else "NO_BIOMETRICS"

        val startAuthentication = {
            if (isBiometricAvailable && authJob?.isActive != true) {
                uiStatusState = "READY"
                authJob = lifecycleScope.launch {
                    val result = authenticator.authenticate()
                    viewModel.onAuthenticationResult(packageName, result)
                    when (result) {
                        AuthenticationResult.SUCCESS -> {
                            isAuthenticated = true
                            finish()
                        }
                        AuthenticationResult.FAILED,
                        AuthenticationResult.ERROR -> {
                            uiStatusState = "FAILED"
                        }
                        AuthenticationResult.CANCELLED -> {
                            goHome()
                        }
                        AuthenticationResult.UNAVAILABLE -> {
                            uiStatusState = "NO_BIOMETRICS"
                        }
                    }
                }
            }
        }

        startAuthentication()

        setContent {
            WCMPTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppLockScreen(
                        packageName = packageName,
                        applicationName = applicationName,
                        isBiometricAvailable = isBiometricAvailable,
                        uiStatus = uiStatusState,
                        onAuthenticateClick = {
                            startAuthentication()
                        },
                        onCancelClick = {
                            authJob?.cancel()
                            viewModel.onAuthenticationResult(
                                packageName,
                                AuthenticationResult.CANCELLED
                            )
                            goHome()
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    @Deprecated("Deprecated in Java")
    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        goHome()
    }
}
