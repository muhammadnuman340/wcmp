package com.numan.wcmp.ui.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.numan.wcmp.ui.theme.spacing

@Composable
fun WcmpCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun SecurityCard(
    modifier: Modifier = Modifier,
    isCritical: Boolean = false,
    isWarning: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = when {
        isCritical -> MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
        isWarning -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }
    
    val bgColor = when {
        isCritical -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        isWarning -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surface
    }

    WcmpCard(
        modifier = modifier,
        backgroundColor = bgColor,
        borderColor = borderColor,
        content = content
    )
}
