package com.numan.wcmp.feature.vault.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numan.wcmp.feature.vault.model.VaultEvidence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultDetailsSheet(
    evidenceUiModel: com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "EVIDENCE DETAILS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            DetailRow("Title", evidenceUiModel.evidence.title)
            DetailRow("Type", evidenceUiModel.evidence.type.name)
            DetailRow("Related Event", evidenceUiModel.evidence.relatedEvent)
            DetailRow("Captured At", evidenceUiModel.evidence.timestamp)
            
            if (evidenceUiModel.evidence.latitude != null && evidenceUiModel.evidence.longitude != null) {
                DetailRow("Location", String.format("%.6f, %.6f", evidenceUiModel.evidence.latitude, evidenceUiModel.evidence.longitude))
                if (evidenceUiModel.evidence.accuracyMeters != null) {
                    DetailRow("Accuracy", "${evidenceUiModel.evidence.accuracyMeters} meters")
                }
            }
            
            DetailRow("File Size", evidenceUiModel.evidence.size)
            DetailRow("Evidence ID", evidenceUiModel.evidence.id)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "STORAGE STATUS",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Security State",
                    modifier = Modifier.weight(0.4f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                val statusText = evidenceUiModel.health.name
                val statusColor = when (evidenceUiModel.health) {
                    com.numan.wcmp.domain.evidence.health.EvidenceHealth.HEALTHY -> MaterialTheme.colorScheme.tertiary
                    com.numan.wcmp.domain.evidence.health.EvidenceHealth.LEGACY -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.error
                }
                
                Text(
                    text = statusText,
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = statusColor
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Encryption",
                    modifier = Modifier.weight(0.4f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                val encryptionDesc = if (evidenceUiModel.health == com.numan.wcmp.domain.evidence.health.EvidenceHealth.HEALTHY || evidenceUiModel.evidence.isEncrypted) "AES-GCM (v${evidenceUiModel.evidence.encryptionVersion ?: 1})" else "None"
                Text(
                    text = encryptionDesc,
                    modifier = Modifier.weight(0.6f),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (evidenceUiModel.health == com.numan.wcmp.domain.evidence.health.EvidenceHealth.HEALTHY || evidenceUiModel.evidence.isEncrypted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp)) // Safe bottom padding
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(0.4f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                modifier = Modifier.weight(0.6f),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}


