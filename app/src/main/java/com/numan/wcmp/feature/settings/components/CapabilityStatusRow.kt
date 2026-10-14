package com.numan.wcmp.feature.settings.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CapabilityStatusRow(
    label: String,
    statusText: String,
    modifier: Modifier = Modifier
) {
    val statusColor = when (statusText.uppercase()) {
        "READY", "CONFIGURED", "ON" -> MaterialTheme.colorScheme.tertiary
        "CONFIGURATION ONLY", "DEVICE ONLY", "DEFAULT", "WCMP BLUE", "DARK" -> MaterialTheme.colorScheme.primary
        "NOT CONNECTED", "NOT IMPLEMENTED", "OFF" -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        "ERROR" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = statusText.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = statusColor
        )
    }
}

