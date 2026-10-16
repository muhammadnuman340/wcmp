package com.numan.wcmp

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.numan.wcmp.data.local.preferences.WcmpPreferences

/**
 * Main Activity for WCMP - Who Checked My Phone.
 * Hosts the Jetpack Compose foundation architecture.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // SEC-01-01: Globally enforce FLAG_SECURE to prevent recent-task thumbnails and screen captures
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        enableEdgeToEdge()
        setContent {
            val preferences = remember { WcmpPreferences(applicationContext) }
            val themePreference by preferences.theme.collectAsState(initial = "System Default")
            val isDarkTheme = when (themePreference) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }
            WcmpApp(darkTheme = isDarkTheme)
        }
    }
}
