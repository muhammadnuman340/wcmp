package com.numan.wcmp.feature.events.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
fun EventSummarySection(
    modifier: Modifier = Modifier,
    totalEvents: Int,
    evidenceCount: Int,
    todayCount: Int,
    activeCount: Int
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        WcmpCard(
            modifier = Modifier.weight(1f),
            contentPadding = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(value = totalEvents.toString(), label = "TOTAL EVENTS", isPrimary = true)
                SummaryItem(value = evidenceCount.toString(), label = "EVIDENCE")
            }
        }
        
        WcmpCard(
            modifier = Modifier.weight(1f),
            contentPadding = 16.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(value = todayCount.toString(), label = "TODAY")
                SummaryItem(value = activeCount.toString(), label = "ACTIVE")
            }
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
        horizontalAlignment = Alignment.Start
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
                fontSize = 9.sp
            ),
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

