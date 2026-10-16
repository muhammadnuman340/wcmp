package com.numan.wcmp.feature.home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.numan.wcmp.core.evidence.SampleEvidenceSeeder
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceTrigger
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.icons.Camera
import com.numan.wcmp.ui.icons.ChevronRight
import com.numan.wcmp.ui.icons.Mic
import com.numan.wcmp.ui.icons.Navigation
import com.numan.wcmp.ui.icons.WcmpIcons
import kotlinx.coroutines.launch

@Composable
fun EvidenceSummarySection(
    modifier: Modifier = Modifier,
    onNavigateToVault: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = WcmpRepositoryProvider.getSecurityRepository(context)
    val coordinator = WcmpRepositoryProvider.getEvidenceCaptureCoordinator(context)
    val vaultEvidence by repository.observeVaultEvidence().collectAsState(initial = emptyList())

    val photoCount = vaultEvidence.count { it.type == com.numan.wcmp.feature.vault.model.VaultEvidenceType.PHOTO }
    val audioCount = vaultEvidence.count { it.type == com.numan.wcmp.feature.vault.model.VaultEvidenceType.AUDIO }
    val locationCount = vaultEvidence.count { it.type == com.numan.wcmp.feature.vault.model.VaultEvidenceType.LOCATION }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 4.dp, end = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "EVIDENCE SUMMARY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onNavigateToVault,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Open Evidence Vault",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = WcmpIcons.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        WcmpCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 20.dp,
            onClick = onNavigateToVault
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryItem(title = "Photos", count = photoCount)
                    SummaryItem(title = "Audio", count = audioCount)
                    SummaryItem(title = "Location", count = locationCount)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "QUICK EVIDENCE CAPTURE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                coordinator.requestCapture(
                                    EvidenceCaptureRequest(
                                        trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                        preferredType = EvidenceType.FRONT_CAMERA_PHOTO
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = WcmpIcons.Camera,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Photo", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                coordinator.requestCapture(
                                    EvidenceCaptureRequest(
                                        trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                        preferredType = EvidenceType.AUDIO_CLIP
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = WcmpIcons.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Audio", style = MaterialTheme.typography.labelSmall)
                    }

                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                coordinator.requestCapture(
                                    EvidenceCaptureRequest(
                                        trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                        preferredType = EvidenceType.LOCATION_STAMP
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = WcmpIcons.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "GPS", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (com.numan.wcmp.BuildConfig.DEBUG) {
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                val count = SampleEvidenceSeeder.seed(context)
                                Toast.makeText(context, "Added $count verified evidence records", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ Generate Sample Evidence Set",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryItem(title: String, count: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
