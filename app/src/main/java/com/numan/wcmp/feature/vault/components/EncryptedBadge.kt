package com.numan.wcmp.feature.vault.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class EncryptedBadgeState {
    ENCRYPTED,
    LEGACY,
    UNAVAILABLE,
    INTEGRITY_ERROR
}

@Composable
fun EncryptedBadge(
    modifier: Modifier = Modifier,
    state: EncryptedBadgeState
) {
    val (text, textColor, bgColor) = when (state) {
        EncryptedBadgeState.ENCRYPTED -> Triple("● ENCRYPTED", MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
        EncryptedBadgeState.LEGACY -> Triple("LEGACY", MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
        EncryptedBadgeState.UNAVAILABLE -> Triple("UNAVAILABLE", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
        EncryptedBadgeState.INTEGRITY_ERROR -> Triple("INTEGRITY ERROR", MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            ),
            color = textColor
        )
    }
}

