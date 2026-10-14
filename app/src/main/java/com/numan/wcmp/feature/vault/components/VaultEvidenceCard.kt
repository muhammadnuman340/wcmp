package com.numan.wcmp.feature.vault.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numan.wcmp.feature.vault.model.VaultEvidence
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.icons.WcmpIcons

@Composable
fun VaultEvidenceCard(
    modifier: Modifier = Modifier,
    evidenceUiModel: com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel,
    onClick: () -> Unit
) {
    WcmpCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (evidenceUiModel.evidence.type) {
                        VaultEvidenceType.PHOTO -> WcmpIcons.Eye // Placeholder icon
                        VaultEvidenceType.VIDEO -> WcmpIcons.History // Placeholder icon
                        VaultEvidenceType.EVENT -> WcmpIcons.ShieldCheck
                        VaultEvidenceType.LOCATION -> WcmpIcons.Radar
                        else -> WcmpIcons.Lock
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = evidenceUiModel.evidence.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = evidenceUiModel.evidence.type.name,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = evidenceUiModel.evidence.relatedEvent,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    EncryptedBadge(
                        state = when(evidenceUiModel.health) { com.numan.wcmp.domain.evidence.health.EvidenceHealth.HEALTHY -> EncryptedBadgeState.ENCRYPTED; com.numan.wcmp.domain.evidence.health.EvidenceHealth.LEGACY -> EncryptedBadgeState.LEGACY; else -> EncryptedBadgeState.INTEGRITY_ERROR }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = evidenceUiModel.evidence.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = evidenceUiModel.evidence.size,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

