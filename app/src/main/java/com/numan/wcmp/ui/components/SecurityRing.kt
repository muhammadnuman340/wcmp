package com.numan.wcmp.ui.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.numan.wcmp.ui.icons.WcmpIcons

enum class SecurityRingState {
    ACTIVE,
    LIMITED,
    STOPPED,
    REQUIRES_ACCESS,
    ERROR
}

@Composable
fun SecurityRing(
    state: SecurityRingState,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_transition")
    
    val isAnimated = state == SecurityRingState.ACTIVE || state == SecurityRingState.LIMITED

    // Subtle rotation animation for the outer ring
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isAnimated) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Pulse animation for the glow effect
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = if (isAnimated) 0.2f else 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    val primaryColor = when (state) {
        SecurityRingState.ACTIVE -> MaterialTheme.colorScheme.tertiary // Protection green
        SecurityRingState.LIMITED, SecurityRingState.REQUIRES_ACCESS -> MaterialTheme.colorScheme.secondary // Warning blue or warning orange
        SecurityRingState.ERROR -> MaterialTheme.colorScheme.error
        SecurityRingState.STOPPED -> MaterialTheme.colorScheme.outline
    }
    
    val warningColor = if (MaterialTheme.colorScheme.background.red < 0.5f) {
        Color(0xFFFFB84D) // WcmpWarning (dark mode)
    } else {
        Color(0xFFD97706) // Amber 600 (light mode)
    }

    val actualColor = if (state == SecurityRingState.LIMITED || state == SecurityRingState.REQUIRES_ACCESS) {
        warningColor
    } else {
        primaryColor
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Glowing background
        Box(
            modifier = Modifier
                .fillMaxSize(0.7f)
                .clip(CircleShape)
                .background(actualColor.copy(alpha = pulseAlpha))
        )

        // Animated Ring Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val canvasSize = this.size.minDimension
            val radius = (canvasSize - strokeWidth) / 2

            // Background track ring
            drawArc(
                color = actualColor.copy(alpha = 0.2f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Main continuous ring
            val sweep = when (state) {
                SecurityRingState.ACTIVE -> 280f
                SecurityRingState.LIMITED -> 180f
                SecurityRingState.STOPPED -> 0f
                SecurityRingState.REQUIRES_ACCESS -> 90f
                SecurityRingState.ERROR -> 360f
            }
            
            if (sweep > 0f) {
                drawArc(
                    color = actualColor,
                    startAngle = rotation - 90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round
                    )
                )
            }
        }

        // Inner Shield Icon
        Icon(
            imageVector = WcmpIcons.Shield,
            contentDescription = null,
            tint = actualColor,
            modifier = Modifier.size(48.dp)
        )
    }
}
