package com.numan.wcmp.feature.home
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.numan.wcmp.ui.components.SecurityRing
import com.numan.wcmp.ui.components.SecurityRingState
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.theme.spacing

@Composable
fun SecurityCenter(
    modifier: Modifier = Modifier,
    isProtected: Boolean = true,
    activeTime: String = "Started 2h 18m ago",
    eventsCount: Int = 3,
    evidenceCount: Int = 14
) {
    val state = if (isProtected) SecurityRingState.ACTIVE else SecurityRingState.STOPPED
    val statusColor = if (isProtected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
    
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        WcmpCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 24.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Left Side: Ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SecurityRing(
                        state = state,
                        size = 120.dp
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                // Right Side: Stats
                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    // Events
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = eventsCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "EVENTS",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Evidence
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = evidenceCount.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = "EVIDENCE",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 14.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Status Duration Bar
        WcmpCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 16.dp,
            backgroundColor = statusColor.copy(alpha = 0.1f),
            borderColor = statusColor.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isProtected) "PROTECTION ACTIVE" else "SYSTEM CURRENTLY UNARMED",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = statusColor
                        )
                        if (isProtected) {
                            Text(
                                text = "PROTECTED FOR $activeTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Chevron icon
                Icon(
                    imageVector = com.numan.wcmp.ui.icons.WcmpIcons.ArrowBack, // Wait, maybe right arrow? 
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp) // We'll rotate it if needed, or omit it. Let's omit or use a generic one
                )
            }
        }
    }
}
