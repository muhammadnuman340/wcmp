package com.numan.wcmp.feature.vault.components
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
fun VaultSummary(
    modifier: Modifier = Modifier,
    evidenceCount: Int,
    photosCount: Int,
    audioCount: Int,
    locationCount: Int,
    eventsCount: Int
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
            SummaryItem(value = evidenceCount.toString(), label = "TOTAL", isPrimary = true)
            SummaryItem(value = photosCount.toString(), label = "PHOTOS")
            SummaryItem(value = audioCount.toString(), label = "AUDIO")
            SummaryItem(value = locationCount.toString(), label = "LOCATION")
            SummaryItem(value = eventsCount.toString(), label = "EVENTS")
        }
    }
}

@Composable
private fun SummaryItem(
    value: String,
    label: String,
    isPrimary: Boolean = false
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
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

