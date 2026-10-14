package com.numan.wcmp.feature.applock
import androidx.compose.material3.MaterialTheme

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.numan.wcmp.core.security.AndroidWcmpAuthenticator
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.protection.AuthenticationResult
import com.numan.wcmp.ui.theme.WCMPTheme
import kotlinx.coroutines.launch

class AppLockActivity : FragmentActivity() {

    private val viewModel: AppLockViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appLockController = WcmpRepositoryProvider.getAppLockController(applicationContext)
                return AppLockViewModel(appLockController) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Prevent screenshots and screen recording on the lock screen
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        
        val packageName = intent.getStringExtra("EXTRA_PACKAGE_NAME") ?: return finish()
        val applicationName = intent.getStringExtra("EXTRA_APPLICATION_NAME")
        
        val authenticator = AndroidWcmpAuthenticator(this)
        
        val biometricManager = androidx.biometric.BiometricManager.from(this)
        val authenticators = androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val isBiometricAvailable = biometricManager.canAuthenticate(authenticators) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
        
        val startAuthentication = {
            if (isBiometricAvailable) {
                lifecycleScope.launch {
                    val result = authenticator.authenticate()
                    viewModel.onAuthenticationResult(packageName, result)
                    if (result == AuthenticationResult.SUCCESS) {
                        finish()
                    }
                }
            }
        }
        
        // Auto-prompt on launch
        startAuthentication()
        
        // Disable back button and outside touches natively as much as possible
        // (Handled via Compose BackHandler and full-screen surface)
        
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
                        onAuthenticateClick = {
                            startAuthentication()
                        },
                        onCancelClick = {
                            viewModel.onAuthenticationResult(packageName, AuthenticationResult.CANCELLED)
                            goHome()
                        }
                    )
                }
            }
        }
    }
    
    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
    
    // Prevent back button from bypassing
    @Deprecated("Deprecated in Java")
    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        goHome()
    }
}
