package com.numan.wcmp.feature.vault.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel
import com.numan.wcmp.ui.components.DangerWcmpButton
import com.numan.wcmp.ui.components.PrimaryWcmpButton
import com.numan.wcmp.ui.components.SecondaryWcmpButton
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar
import com.numan.wcmp.ui.icons.*






@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VaultEvidenceDetails(
    evidenceUiModel: VaultEvidenceUiModel,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    var showPreview by remember { mutableStateOf(false) }

    if (showPreview) {
        com.numan.wcmp.feature.vault.preview.EvidencePreviewScreen(
            evidenceUiModel = evidenceUiModel,
            onClose = { showPreview = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            WcmpTopBar(
                title = "Evidence Details",
                subtitle = null,
                onBackClick = onBack
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
            ) {
                item {
                    EvidenceHero(evidenceUiModel)
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                        PrimaryWcmpButton(
                            text = "VIEW EVIDENCE",
                            onClick = { showPreview = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    SecurityInspectorCard(evidenceUiModel)
                }

                item {
                    EvidenceInformationCard(evidenceUiModel)
                }

                item {
                    SourceAndContextCard(evidenceUiModel)
                }

                item {
                    SecurityDetailsCard(evidenceUiModel)
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        DangerWcmpButton(
                            text = "Delete Evidence",
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        if (showDeleteConfirmation) {
            DeleteConfirmationDialog(
                onDismiss = { showDeleteConfirmation = false },
                onConfirm = {
                    showDeleteConfirmation = false
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun EvidenceHero(evidenceUiModel: VaultEvidenceUiModel) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentPadding = 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Simulated preview area (real app would load decrypted thumbnail here safely)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (evidenceUiModel.health == EvidenceHealth.HEALTHY || evidenceUiModel.health == EvidenceHealth.LEGACY) {
                    Icon(
                        imageVector = when (evidenceUiModel.evidence.type) {
                            VaultEvidenceType.PHOTO -> WcmpIcons.Eye
                            VaultEvidenceType.AUDIO -> WcmpIcons.Mic
                            VaultEvidenceType.LOCATION -> WcmpIcons.LocationOn
                            else -> WcmpIcons.ShieldCheck
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = WcmpIcons.ShieldAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PREVIEW UNAVAILABLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Hero metadata
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = evidenceUiModel.evidence.title.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = evidenceUiModel.evidence.relatedEvent,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = evidenceUiModel.evidence.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                BadgeForHealth(evidenceUiModel.health)
            }
        }
    }
}

@Composable
private fun SecurityInspectorCard(evidenceUiModel: VaultEvidenceUiModel) {
    val (status, color, subtitle) = when (evidenceUiModel.health) {
        EvidenceHealth.HEALTHY -> Triple("ENCRYPTED", MaterialTheme.colorScheme.tertiary, "AES-GCM â€¢ Android Keystore")
        EvidenceHealth.LEGACY -> Triple("LEGACY EVIDENCE", MaterialTheme.colorScheme.secondary, "Stored as plaintext")
        EvidenceHealth.INTEGRITY_ERROR -> Triple("INTEGRITY ERROR", MaterialTheme.colorScheme.error, "Evidence could not be verified")
        EvidenceHealth.MISSING_FILE -> Triple("FILE MISSING", MaterialTheme.colorScheme.error, "The encrypted file was not found")
        EvidenceHealth.KEY_UNAVAILABLE -> Triple("KEY UNAVAILABLE", MaterialTheme.colorScheme.error, "Decryption key cannot be accessed")
        else -> Triple("UNKNOWN", MaterialTheme.colorScheme.onSurfaceVariant, "Security state is unknown")
    }

    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentPadding = 20.dp,
        backgroundColor = color.copy(alpha = 0.05f),
        borderColor = color.copy(alpha = 0.2f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (evidenceUiModel.health == EvidenceHealth.HEALTHY) WcmpIcons.ShieldCheck else WcmpIcons.ShieldAlert,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SECURITY STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (evidenceUiModel.health == EvidenceHealth.HEALTHY) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(WcmpIcons.Check, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("VERIFIED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = color
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EvidenceInformationCard(evidenceUiModel: VaultEvidenceUiModel) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = "EVIDENCE INFORMATION",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            InfoRow("Type", evidenceUiModel.evidence.type.name.lowercase().replaceFirstChar { it.uppercase() })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            InfoRow("Timestamp", evidenceUiModel.evidence.timestamp)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            InfoRow("Size", evidenceUiModel.evidence.size)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            
            val healthText = when (evidenceUiModel.health) {
                EvidenceHealth.HEALTHY -> "Encrypted"
                EvidenceHealth.LEGACY -> "Legacy Unencrypted"
                EvidenceHealth.MISSING_FILE -> "Missing"
                EvidenceHealth.KEY_UNAVAILABLE -> "Key Unavailable"
                EvidenceHealth.INTEGRITY_ERROR -> "Integrity Error"
                else -> "Unknown"
            }
            InfoRow(
                "Health State", 
                healthText,
                valueColor = if (evidenceUiModel.health == EvidenceHealth.HEALTHY) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SourceAndContextCard(evidenceUiModel: VaultEvidenceUiModel) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = "SOURCE & CONTEXT",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            InfoRow("Event", evidenceUiModel.evidence.relatedEvent)
            if (evidenceUiModel.evidence.sessionId != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                InfoRow("Session ID", evidenceUiModel.evidence.sessionId.take(12) + "...")
            }
        }
    }
}

@Composable
private fun SecurityDetailsCard(evidenceUiModel: VaultEvidenceUiModel) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Text(
            text = "SECURITY DETAILS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SecurityDetailColumn(
                    title = "Encryption",
                    status = if (evidenceUiModel.health == EvidenceHealth.HEALTHY) "VERIFIED" else if (evidenceUiModel.health == EvidenceHealth.LEGACY) "LEGACY" else "ERROR",
                    isGood = evidenceUiModel.health == EvidenceHealth.HEALTHY
                )
                SecurityDetailColumn(
                    title = "Integrity",
                    status = if (evidenceUiModel.health == EvidenceHealth.HEALTHY) "VERIFIED" else if (evidenceUiModel.health == EvidenceHealth.INTEGRITY_ERROR) "FAILED" else "UNVERIFIED",
                    isGood = evidenceUiModel.health == EvidenceHealth.HEALTHY
                )
                SecurityDetailColumn(
                    title = "Storage",
                    status = if (evidenceUiModel.health == EvidenceHealth.MISSING_FILE) "MISSING" else "AVAILABLE",
                    isGood = evidenceUiModel.health != EvidenceHealth.MISSING_FILE
                )
            }
        }
    }
}

@Composable
private fun SecurityDetailColumn(title: String, status: String, isGood: Boolean) {
    val color = if (isGood) MaterialTheme.colorScheme.tertiary else if (status == "LEGACY" || status == "UNVERIFIED") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isGood) WcmpIcons.Check else WcmpIcons.ShieldAlert,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = valueColor
        )
    }
}

@Composable
private fun BadgeForHealth(health: EvidenceHealth) {
    val badgeColor = when (health) {
        EvidenceHealth.HEALTHY -> MaterialTheme.colorScheme.tertiary
        EvidenceHealth.LEGACY -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    val badgeText = when (health) {
        EvidenceHealth.HEALTHY -> "ENCRYPTED"
        EvidenceHealth.LEGACY -> "LEGACY"
        EvidenceHealth.INTEGRITY_ERROR -> "INTEGRITY ERROR"
        EvidenceHealth.MISSING_FILE -> "FILE MISSING"
        EvidenceHealth.KEY_UNAVAILABLE -> "KEY LOST"
        else -> "UNKNOWN"
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (health == EvidenceHealth.HEALTHY) WcmpIcons.ShieldCheck else WcmpIcons.ShieldAlert,
            contentDescription = null,
            tint = badgeColor,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = badgeText,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
            color = badgeColor
        )
    }
}

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Delete Evidence?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "This permanently removes the selected evidence. This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            DangerWcmpButton(
                text = "DELETE",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            SecondaryWcmpButton(
                text = "CANCEL",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

