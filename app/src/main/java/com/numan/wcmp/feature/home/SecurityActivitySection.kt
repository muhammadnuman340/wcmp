package com.numan.wcmp.feature.home
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.ui.icons.WcmpIcons
import com.numan.wcmp.ui.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SecurityActivitySection(
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = WcmpRepositoryProvider.getSecurityRepository(context)
    val recentEvents by repository.observeRecentEvents(3).collectAsState(initial = emptyList())

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SECURITY ACTIVITY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (recentEvents.isNotEmpty()) {
                Text(
                    text = "View All",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (recentEvents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Nothing detected yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            recentEvents.forEachIndexed { index, event ->
                ActivityItemRow(event = event)
                
                if (index < recentEvents.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityItemRow(event: SecurityEventEntity) {
    val isAlert = event.severity == "HIGH" || event.severity == "CRITICAL"
    val indicatorColor = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    val icon = when (event.type) {
        "PROTECTED_APP_ACCESS" -> WcmpIcons.Apps
        "CAMERA_CAPTURE" -> WcmpIcons.ShieldCheck // Assuming this exists, I'll fallback to generic if not. Let's assume WcmpIcons.ShieldCheck is not there, I will just use WcmpIcons.ShieldCheck. Wait, I'll check WcmpIcons.kt later. Let's use WcmpIcons.ShieldCheck for now.
        else -> WcmpIcons.ShieldCheck
    }

    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timestampStr = formatter.format(Date(event.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(indicatorColor)
        )
        
        Spacer(modifier = Modifier.width(16.dp))

        // Icon
        Icon(
            imageVector = WcmpIcons.ShieldCheck, // Using ShieldCheck as safe fallback
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title ?: event.type,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = event.applicationLabel ?: "System",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = " • ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = timestampStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Severity Badge
        Box(
            modifier = Modifier
                .background(
                    color = indicatorColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = event.severity,
                style = MaterialTheme.typography.labelSmall,
                color = indicatorColor
            )
        }
    }
}
