package com.numan.wcmp.feature.vault.preview

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel
import com.numan.wcmp.ui.components.PrimaryWcmpButton
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar
import com.numan.wcmp.ui.icons.*
import com.numan.wcmp.ui.icons.WcmpIcons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning

@Composable
fun EvidencePreviewScreen(
    evidenceUiModel: VaultEvidenceUiModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EvidencePreviewViewModel = viewModel(
        factory = EvidencePreviewViewModel.Factory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()

        val windowSize = com.numan.wcmp.ui.adaptive.rememberWindowSizeClass()
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.roundToPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.roundToPx() }

    // Load evidence on first composition
    LaunchedEffect(evidenceUiModel) {
        viewModel.loadEvidence(evidenceUiModel, screenWidthPx, screenHeightPx)
    }

    // Ensure cleanup when screen exits
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanup()
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                viewModel.pausePlayback()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WcmpTopBar(
            title = "Evidence Preview",
            subtitle = null,
            onBackClick = onClose
        )

        val previewContent = @Composable {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(targetState = uiState, label = "PreviewContent") { state ->
                    when (state) {
                        is EvidencePreviewState.Idle, is EvidencePreviewState.Preparing -> {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                        is EvidencePreviewState.Decrypting -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "DECRYPTING SECURELY...",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                        is EvidencePreviewState.ImageReady -> {
                            val androidImage = state.image as? AndroidPreviewImageData
                            if (androidImage != null) {
                                Image(
                                    bitmap = androidImage.bitmap.asImageBitmap(),
                                    contentDescription = "Decrypted photo evidence",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                ErrorState("PREVIEW FAILED", "Unsupported image format.")
                            }
                        }
                        is EvidencePreviewState.AudioReady -> {
                            EvidenceAudioPlayer(
                                isPlaying = isPlaying,
                                playbackPosition = playbackPosition,
                                durationMs = state.durationMs,
                                onPlayPauseToggle = { viewModel.togglePlayPause() },
                                onSeek = { viewModel.seekTo(it) }
                            )
                        }
                        is EvidencePreviewState.LocationReady -> {
                            EvidenceLocationPreview(state)
                        }
                        is EvidencePreviewState.Failed -> {
                            ErrorState("PREVIEW FAILED", state.message)
                        }
                        is EvidencePreviewState.IntegrityError -> {
                            ErrorState("INTEGRITY ERROR", "Evidence could not be verified.")
                        }
                        is EvidencePreviewState.KeyUnavailable -> {
                            ErrorState("KEY UNAVAILABLE", "Decryption key cannot be accessed.")
                        }
                        is EvidencePreviewState.MissingFile -> {
                            ErrorState("EVIDENCE UNAVAILABLE", "The encrypted file was not found.")
                        }
                        is EvidencePreviewState.LegacyEvidence -> {
                            ErrorState("LEGACY / UNENCRYPTED", "This evidence was captured before encryption was enabled.")
                        }
                        is EvidencePreviewState.MigrationRequired -> {
                            ErrorState("MIGRATION REQUIRED", "Evidence format must be upgraded.")
                        }
                    }
                }
            }
        }

        val detailsContent = @Composable {
            SecurityInspector(evidenceUiModel)
            Spacer(modifier = Modifier.height(16.dp))
            EvidenceInformation(evidenceUiModel)
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryWcmpButton(
                text = "CLOSE PREVIEW",
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }

        if (windowSize == com.numan.wcmp.ui.adaptive.WindowSizeClass.EXPANDED) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight().padding(vertical = 24.dp)
                ) {
                    previewContent()
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    modifier = Modifier.width(360.dp).fillMaxHeight().padding(vertical = 24.dp)
                ) {
                    detailsContent()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    previewContent()
                }
                Spacer(modifier = Modifier.height(24.dp))
                detailsContent()
            }
        }
    }
}

@Composable
private fun EvidenceAudioPlayer(
    isPlaying: Boolean,
    playbackPosition: Long,
    durationMs: Long?,
    onPlayPauseToggle: () -> Unit,
    onSeek: (Long) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = WcmpIcons.Mic,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "AUDIO EVIDENCE",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                formatTime(playbackPosition),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Slider(
                value = if (durationMs != null && durationMs > 0) playbackPosition.toFloat() / durationMs.toFloat() else 0f,
                onValueChange = { fraction -> 
                    if (durationMs != null) onSeek((fraction * durationMs).toLong())
                },
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            )
            
            Text(
                formatTime(durationMs ?: 0L),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        IconButton(
            onClick = onPlayPauseToggle,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Close else Icons.Filled.PlayArrow, // Replace Close with Pause if you have one, using typical icon
                contentDescription = if (isPlaying) "Pause encrypted audio evidence" else "Play encrypted audio evidence",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun EvidenceLocationPreview(state: EvidencePreviewState.LocationReady) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Icon(
            imageVector = WcmpIcons.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "LOCATION EVIDENCE",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        LocationRow("Latitude", String.format("%.5f", state.latitude))
        Spacer(modifier = Modifier.height(12.dp))
        LocationRow("Longitude", String.format("%.5f", state.longitude))
        Spacer(modifier = Modifier.height(12.dp))
        LocationRow("Accuracy", state.accuracy?.let { "ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â±${it.toInt()} m" } ?: "Unknown")
    }
}

@Composable
private fun LocationRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        Text(value, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun ErrorState(title: String, message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun SecurityInspector(evidenceUiModel: VaultEvidenceUiModel) {
    val isLocation = evidenceUiModel.evidence.type == VaultEvidenceType.LOCATION
    val isEncrypted = evidenceUiModel.health == EvidenceHealth.HEALTHY

    WcmpCard(contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val color = if (isEncrypted && !isLocation) MaterialTheme.colorScheme.tertiary else if (isLocation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isEncrypted || isLocation) WcmpIcons.ShieldCheck else Icons.Filled.Warning,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                if (isLocation) {
                    Text("SECURE CAPTURE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                    Text("Metadata stored locally", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (isEncrypted) {
                    Text("ENCRYPTED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.tertiary)
                    Text("AES-GCM ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€šÃ‚Â¢ Integrity Verified", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("UNPROTECTED", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                    Text("Evidence is not encrypted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EvidenceInformation(evidenceUiModel: VaultEvidenceUiModel) {
    WcmpCard(contentPadding = 16.dp) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Timestamp", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(evidenceUiModel.evidence.timestamp, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Source", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(evidenceUiModel.evidence.relatedEvent, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Type", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(evidenceUiModel.evidence.type.name, color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}










