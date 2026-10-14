package com.numan.wcmp.feature.evidence.capture.location

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.ui.haptics.rememberWcmpHaptics
import kotlinx.coroutines.delay

@Composable
fun LocationCaptureScreen(
    onFinished: () -> Unit,
    viewModel: LocationCaptureViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val haptics = rememberWcmpHaptics()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        if (granted) {
            viewModel.onPermissionGranted()
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(Unit) {
        val fineStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        
        if (fineStatus == android.content.pm.PackageManager.PERMISSION_GRANTED || 
            coarseStatus == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.onPermissionGranted()
        } else {
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is LocationCaptureUiState.Success) {
            haptics.success()
            delay(1500)
            onFinished()
        } else if (uiState is LocationCaptureUiState.Cancelled || uiState is LocationCaptureUiState.Failed) {
            if (uiState is LocationCaptureUiState.Failed) haptics.error()
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
                        LocationDisplayContent(
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
                        LocationControlArea(
                            uiState = uiState,
                            onCapture = {
                                haptics.heavyClick()
                                viewModel.startCapture(context)
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
                        LocationDisplayContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onFinished = onFinished
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    LocationControlArea(
                        uiState = uiState,
                        onCapture = {
                            haptics.heavyClick()
                            viewModel.startCapture(context)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationDisplayContent(
    uiState: LocationCaptureUiState,
    viewModel: LocationCaptureViewModel,
    onFinished: () -> Unit
) {
    when (uiState) {
        is LocationCaptureUiState.Idle -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "LOCATION READY",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ready to record GPS coordinates stamp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is LocationCaptureUiState.PermissionRequired -> {
            PermissionCard(
                message = "Location permission is required to capture location evidence.",
                onDismiss = {
                    viewModel.cancel()
                    onFinished()
                }
            )
        }
        is LocationCaptureUiState.Acquiring -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "ACQUIRING LOCATION...",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
        is LocationCaptureUiState.Success -> {
            SuccessCard()
        }
        is LocationCaptureUiState.Failed -> {
            ErrorCard(message = uiState.message)
        }
        is LocationCaptureUiState.Cancelled -> {
            Text(
                text = "CAPTURE CANCELLED", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun LocationControlArea(
    uiState: LocationCaptureUiState,
    onCapture: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = uiState is LocationCaptureUiState.Idle,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "LocationControls"
        ) { isReady ->
            if (isReady) {
                Button(
                    onClick = onCapture,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("CAPTURE LOCATION", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            } else {
                Spacer(modifier = Modifier.height(56.dp))
            }
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
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "Secure",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "SECURE CAPTURE",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CaptureStatusIndicator(uiState: LocationCaptureUiState) {
    val statusText = when (uiState) {
        is LocationCaptureUiState.Idle -> "LOCATION READY"
        is LocationCaptureUiState.Acquiring -> "ACQUIRING LOCATION..."
        is LocationCaptureUiState.Success -> "LOCATION CAPTURED"
        is LocationCaptureUiState.PermissionRequired -> "PERMISSION REQUIRED"
        is LocationCaptureUiState.Failed -> "CAPTURE FAILED"
        is LocationCaptureUiState.Cancelled -> "CANCELLED"
    }
    
    val statusColor = when (uiState) {
        is LocationCaptureUiState.Idle -> MaterialTheme.colorScheme.primary
        is LocationCaptureUiState.Acquiring -> MaterialTheme.colorScheme.tertiary
        is LocationCaptureUiState.Success -> MaterialTheme.colorScheme.tertiary
        is LocationCaptureUiState.Failed, is LocationCaptureUiState.PermissionRequired -> MaterialTheme.colorScheme.error
        is LocationCaptureUiState.Cancelled -> MaterialTheme.colorScheme.onSurfaceVariant
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
            text = "LOCATION CAPTURED",
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
