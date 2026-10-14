package com.numan.wcmp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Premium Design System V2 Tokens
val WcmpBackground = Color(0xFF03060B)
val WcmpSurface = Color(0xFF09111B)
val WcmpSurface2 = Color(0xFF0C1723)
val WcmpPrimary = Color(0xFF20BAFF)
val WcmpSecondary = Color(0xFF0879DF)
val WcmpCyan = Color(0xFF69DCFF)
val WcmpProtection = Color(0xFF42E4A0)
val WcmpWarning = Color(0xFFFFB84D)
val WcmpCritical = Color(0xFFFF5C6C)
val WcmpPrimaryText = Color(0xFFF5F9FF)
val WcmpSecondaryText = Color(0xFF9AABBA)
val WcmpMuted = Color(0xFF596B80)

val WcmpDarkColorScheme = darkColorScheme(
    primary = WcmpPrimary,
    onPrimary = WcmpBackground,
    primaryContainer = WcmpSurface2,
    onPrimaryContainer = WcmpCyan,
    secondary = WcmpSecondary,
    onSecondary = WcmpBackground,
    secondaryContainer = WcmpSurface,
    onSecondaryContainer = WcmpSecondary,
    tertiary = WcmpProtection,
    onTertiary = WcmpBackground,
    background = WcmpBackground,
    onBackground = WcmpPrimaryText,
    surface = WcmpSurface,
    onSurface = WcmpPrimaryText,
    surfaceVariant = WcmpSurface2,
    onSurfaceVariant = WcmpSecondaryText,
    outline = WcmpMuted,
    error = WcmpCritical,
    onError = WcmpBackground
)

val WcmpLightColorScheme = lightColorScheme(
    primary = Color(0xFF0284C7), // Sky 600 - crisp, high-contrast security blue on light background
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE), // Sky 100
    onPrimaryContainer = Color(0xFF0369A1), // Sky 700
    secondary = Color(0xFF0369A1), // Blue 700
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0C4A6E),
    tertiary = Color(0xFF059669), // Emerald 600 - accessible high-contrast protection green
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC), // Slate 50
    onBackground = Color(0xFF0F172A), // Slate 900
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF475569), // Slate 600
    outline = Color(0xFFCBD5E1), // Slate 300
    error = Color(0xFFDC2626), // Red 600 - accessible critical error
    onError = Color.White
)

// Cleaned up V1 colors

