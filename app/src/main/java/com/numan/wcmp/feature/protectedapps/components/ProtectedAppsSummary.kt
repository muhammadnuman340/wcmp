package com.numan.wcmp.feature.protectedapps.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numan.wcmp.ui.components.WcmpCard

@Composable
fun ProtectedAppsSummary(
    modifier: Modifier = Modifier,
    protectedCount: Int,
    availableCount: Int,
    activeCount: Int
) {
    WcmpCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryItem(value = protectedCount.toString(), label = "PROTECTED", isPrimary = true)
            SummaryItem(value = availableCount.toString(), label = "AVAILABLE")
            SummaryItem(value = activeCount.toString(), label = "ACTIVE", isSuccess = true)
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    isPrimary: Boolean = false,
    isSuccess: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            ),
            color = when {
                isSuccess -> MaterialTheme.colorScheme.tertiary
                isPrimary -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
        )
    }
}

