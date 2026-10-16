package com.numan.wcmp.ui.theme
import androidx.compose.material3.MaterialTheme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.runtime.CompositionLocalProvider

/**
 * WCMP Theme Foundation
 * Defaults to Dark Security Theme for an authentic cybersecurity aesthetic,
 * while supporting light mode when explicitly configured.
 */
@Composable
fun WCMPTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) WcmpDarkColorScheme else WcmpLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                // Remove status bar color override so it can be transparent
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalWcmpSpacing provides WcmpSpacing()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WcmpTypography,
            shapes = WcmpShapes,
            content = content
        )
    }
}

val MaterialTheme.spacing: WcmpSpacing
    @Composable
    get() = LocalWcmpSpacing.current
