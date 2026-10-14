package com.numan.wcmp.ui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * Premium V2 standard transitions for WCMP.
 * Avoid excessive bouncing or neon styles.
 */
object SecureTransition {

    private const val DURATION_MS = 250

    val enterFadeScale: EnterTransition = fadeIn(tween(DURATION_MS)) + scaleIn(
        initialScale = 0.95f,
        animationSpec = tween(DURATION_MS)
    )

    val exitFadeScale: ExitTransition = fadeOut(tween(DURATION_MS)) + scaleOut(
        targetScale = 1.05f,
        animationSpec = tween(DURATION_MS)
    )

    val enterSlideUp: EnterTransition = slideInVertically(
        initialOffsetY = { it / 8 },
        animationSpec = tween(DURATION_MS)
    ) + fadeIn(tween(DURATION_MS))

    val exitSlideDown: ExitTransition = slideOutVertically(
        targetOffsetY = { it / 8 },
        animationSpec = tween(DURATION_MS)
    ) + fadeOut(tween(DURATION_MS))
}
