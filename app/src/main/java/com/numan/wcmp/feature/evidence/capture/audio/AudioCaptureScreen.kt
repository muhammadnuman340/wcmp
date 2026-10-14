package com.numan.wcmp.feature.evidence.capture.audio

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.ui.haptics.rememberWcmpHaptics
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun AudioCaptureScreen(
    onFinished: () -> Unit,
    viewModel: AudioCaptureViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberWcmpHaptics()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        if (permissionStatus == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.onPermissionGranted()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is AudioCaptureUiState.Success) {
            haptics.success()
            delay(1500)
            onFinished()
        } else if (uiState is AudioCaptureUiState.Cancelled || uiState is AudioCaptureUiState.Failed) {
            if (uiState is AudioCaptureUiState.Failed) haptics.error()
            delay(1000)
            onFinished()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CaptureTopBar(
                onClose = { 
                    viewModel.cancel()
                    onFinished() 
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isWide = maxWidth > 600.dp || (maxWidth > maxHeight && maxWidth > 480.dp)

            if (isWide) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AudioDisplayContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onFinished = onFinished
                        )
                    }

                    Column(
                        modifier = Modifier
                            .width(280.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CaptureStatusIndicator(uiState = uiState)
                        Spacer(modifier = Modifier.height(32.dp))
                        AudioControlArea(
                            uiState = uiState,
                            onStart = {
                                haptics.heavyClick()
                                viewModel.startRecording(context)
                            },
                            onStop = {
                                haptics.heavyClick()
                                viewModel.stopAndSave()
                            }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CaptureStatusIndicator(uiState = uiState)

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        AudioDisplayContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onFinished = onFinished
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AudioControlArea(
                        uiState = uiState,
                        onStart = {
                            haptics.heavyClick()
                            viewModel.startRecording(context)
                        },
                        onStop = {
                            haptics.heavyClick()
                            viewModel.stopAndSave()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioDisplayContent(
    uiState: AudioCaptureUiState,
    viewModel: AudioCaptureViewModel,
    onFinished: () -> Unit
) {
    when (uiState) {
        is AudioCaptureUiState.Idle, is AudioCaptureUiState.Initializing, is AudioCaptureUiState.Finalizing -> {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        is AudioCaptureUiState.PermissionRequired -> {
            PermissionCard(
                message = "Microphone access is required to capture security audio evidence.",
                onDismiss = {
                    viewModel.cancel()
                    onFinished()
                }
            )
        }
        is AudioCaptureUiState.Failed -> {
            val msg = uiState.message
            ErrorCard(message = msg)
        }
        is AudioCaptureUiState.Success -> {
            SuccessCard()
        }
        is AudioCaptureUiState.Ready -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RecordingWaveform(isRecording = false)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "00:00",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Light
                )
            }
        }
        is AudioCaptureUiState.Recording -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RecordingWaveform(isRecording = true)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = formatDuration(uiState.elapsedSeconds * 1000),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        is AudioCaptureUiState.Cancelled -> {
            Text(
                text = "CAPTURE CANCELLED", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun AudioControlArea(
    uiState: AudioCaptureUiState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "AudioControls"
        ) { state ->
            when (state) {
                is AudioCaptureUiState.Ready -> {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("START RECORDING", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                is AudioCaptureUiState.Recording -> {
                    Button(
                        onClick = onStop,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("STOP RECORDING", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    }
                }
                else -> {
                    Spacer(modifier = Modifier.height(56.dp))
                }
            }
        }
    }
}

@Composable
private fun RecordingWaveform(isRecording: Boolean) {
    val barCount = 15
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    // Heights base pattern (symmetrical curve)
    val basePattern = listOf(0.25f, 0.35f, 0.45f, 0.6f, 0.75f, 0.9f, 1.0f, 0.9f, 0.75f, 0.6f, 0.45f, 0.35f, 0.25f, 0.2f, 0.15f)

    Row(
        modifier = Modifier
            .height(80.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val phase = (i * 120) % 600
            val maxMultiplier = basePattern[i.coerceIn(0, basePattern.size - 1)]
            
            val animatedHeight by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = if (isRecording) maxMultiplier else 0.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 350 + phase, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            val barColor = if (isRecording) {
                if (i in 5..9) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            }

            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight(animatedHeight)
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
private fun CaptureTopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Encrypted",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ENCRYPTED CAPTURE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CaptureStatusIndicator(uiState: AudioCaptureUiState) {
    val statusText = when (uiState) {
        is AudioCaptureUiState.Idle, is AudioCaptureUiState.Initializing -> "INITIALIZING AUDIO..."
        is AudioCaptureUiState.Ready -> "MIC READY"
        is AudioCaptureUiState.Recording -> "SECURE CAPTURE ACTIVE"
        is AudioCaptureUiState.Success -> "SAVED SECURELY"
        is AudioCaptureUiState.PermissionRequired -> "PERMISSION REQUIRED"
        is AudioCaptureUiState.Failed -> "CAPTURE FAILED"
        is AudioCaptureUiState.Finalizing -> "FINALIZING"
        is AudioCaptureUiState.Cancelled -> "CANCELLED"
    }
    
    val statusColor = when (uiState) {
        is AudioCaptureUiState.Ready, is AudioCaptureUiState.Finalizing -> MaterialTheme.colorScheme.primary
        is AudioCaptureUiState.Recording, is AudioCaptureUiState.Success -> MaterialTheme.colorScheme.tertiary
        is AudioCaptureUiState.Failed, is AudioCaptureUiState.PermissionRequired -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    AnimatedContent(
        targetState = statusText,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
        label = "StatusText"
    ) { text ->
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = statusColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun PermissionCard(message: String, onDismiss: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("DISMISS")
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessCard() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "EVIDENCE CAPTURED",
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

