package com.numan.wcmp.feature.events.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.numan.wcmp.feature.events.model.SecurityEvent
import com.numan.wcmp.feature.events.model.EventSeverity
import com.numan.wcmp.feature.events.components.getSeverityColor
import com.numan.wcmp.feature.events.components.SeverityBadge
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.PrimaryWcmpButton
import com.numan.wcmp.ui.icons.WcmpIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsSheet(
    event: SecurityEvent,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null,
        modifier = Modifier.fillMaxHeight(0.95f) // Near full screen
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Sheet Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = WcmpIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Event Details",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* TODO Options */ }) {
                    Icon(
                        imageVector = WcmpIcons.Menu,
                        contentDescription = "Options",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Hero Card
                EventHeroCard(event = event)

                Spacer(modifier = Modifier.height(24.dp))

                // Event Information
                SectionHeader("Event Information")
                WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        event.applicationLabel?.let {
                            DetailRow("Application", it)
                        } ?: event.packageName?.let {
                            DetailRow("Application", it)
                        }
                        
                        DetailRow("Event Type", event.title)
                        DetailRow("Severity", event.severity.name, isSeverity = true)
                        DetailRow("Status", event.status ?: "Completed", isStatus = true)
                        
                        event.sessionId?.let {
                            DetailRow("Session ID", "#${it.take(8).uppercase()}...")
                        }
                        
                        if (event.duration.isNotEmpty()) {
                            DetailRow("Duration", event.duration)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Evidence Section
                if (event.evidenceCount > 0) {
                    SectionHeader("Evidence (${event.evidenceCount})")
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Normally this would be a LazyRow mapping actual evidence from the DB.
                        // For now we represent the evidence the event claims to have.
                        if (event.type == "CAMERA_EVIDENCE" || event.evidenceCount > 0) {
                            EvidencePreviewCard(
                                title = "Camera",
                                subtitle = "1 image",
                                icon = WcmpIcons.Apps,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (event.type == "AUDIO_EVIDENCE") {
                            EvidencePreviewCard(
                                title = "Audio",
                                subtitle = event.duration,
                                icon = WcmpIcons.History, // Placeholder for mic
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (event.type == "LOCATION_EVIDENCE") {
                            EvidencePreviewCard(
                                title = "Location",
                                subtitle = "Logged",
                                icon = WcmpIcons.Radar, // Placeholder for map pin
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Security Details
                SectionHeader("Security Details")
                WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = WcmpIcons.ShieldCheck,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Protection was active",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Event was safely captured and logged.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action
                if (event.evidenceCount > 0) {
                    PrimaryWcmpButton(
                        text = "View Full Evidence",
                        onClick = { /* TODO: Navigate to evidence viewer */ },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp)) // Safe bottom padding
            }
        }
    }
}

@Composable
private fun EventHeroCard(event: SecurityEvent) {
    WcmpCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = 20.dp,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            val iconColor = getSeverityColor(event.severity)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = event.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = event.applicationLabel ?: event.packageName ?: event.triggerSource,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (event.evidenceCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = WcmpIcons.Apps,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.evidenceCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}

@Composable
private fun DetailRow(label: String, value: String, isSeverity: Boolean = false, isStatus: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        
        Box(
            modifier = Modifier.weight(0.6f),
            contentAlignment = Alignment.CenterStart
        ) {
            when {
                isSeverity -> {
                    val severityEnum = try { EventSeverity.valueOf(value.uppercase()) } catch (e: Exception) { EventSeverity.INFO }
                    SeverityBadge(severity = severityEnum)
                }
                isStatus -> {
                    val statusColor = MaterialTheme.colorScheme.tertiary
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(statusColor.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = value,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = statusColor
                        )
                    }
                }
                else -> {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
}

@Composable
private fun EvidencePreviewCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    WcmpCard(
        modifier = modifier.height(100.dp),
        contentPadding = 12.dp
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
