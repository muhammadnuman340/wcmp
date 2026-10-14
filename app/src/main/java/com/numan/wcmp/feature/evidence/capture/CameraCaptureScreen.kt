package com.numan.wcmp.feature.evidence.capture

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.ui.haptics.rememberWcmpHaptics
import com.numan.wcmp.ui.animation.SecureTransition
import kotlinx.coroutines.delay

@Composable
fun CameraCaptureScreen(
    onFinished: () -> Unit,
    viewModel: CameraCaptureViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val haptics = rememberWcmpHaptics()

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        }
    }

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
        val permissionStatus = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (permissionStatus == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            viewModel.onPermissionGranted()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is CameraCaptureUiState.Success) {
            haptics.success()
            delay(1500)
            onFinished()
        } else if (uiState is CameraCaptureUiState.Cancelled || uiState is CameraCaptureUiState.Failed) {
            if (uiState is CameraCaptureUiState.Failed) haptics.error()
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
                        CameraViewfinderContent(
                            uiState = uiState,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            viewModel = viewModel,
                            onFinished = onFinished
                        )
                    }

                    Column(
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CaptureStatusIndicator(uiState = uiState)
                        Spacer(modifier = Modifier.height(32.dp))
                        ShutterArea(
                            uiState = uiState,
                            onShutterClick = {
                                haptics.heavyClick()
                                val executor = ContextCompat.getMainExecutor(context)
                                viewModel.capturePhoto(context, cameraController, executor)
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

                    Spacer(modifier = Modifier.height(16.dp))

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
                        CameraViewfinderContent(
                            uiState = uiState,
                            cameraController = cameraController,
                            lifecycleOwner = lifecycleOwner,
                            viewModel = viewModel,
                            onFinished = onFinished
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ShutterArea(
                        uiState = uiState,
                        onShutterClick = {
                            haptics.heavyClick()
                            val executor = ContextCompat.getMainExecutor(context)
                            viewModel.capturePhoto(context, cameraController, executor)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraViewfinderContent(
    uiState: CameraCaptureUiState,
    cameraController: LifecycleCameraController,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    viewModel: CameraCaptureViewModel,
    onFinished: () -> Unit
) {
    when (uiState) {
        is CameraCaptureUiState.Idle, is CameraCaptureUiState.Initializing -> {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        is CameraCaptureUiState.PermissionRequired -> {
            PermissionCard(
                message = uiState.message,
                onDismiss = {
                    viewModel.cancel()
                    onFinished()
                }
            )
        }
        is CameraCaptureUiState.Unavailable, is CameraCaptureUiState.Failed -> {
            val msg = if (uiState is CameraCaptureUiState.Failed) {
                uiState.message
            } else {
                (uiState as CameraCaptureUiState.Unavailable).message
            }
            ErrorCard(message = msg)
        }
        is CameraCaptureUiState.Success -> {
            SuccessCard()
        }
        is CameraCaptureUiState.Ready, is CameraCaptureUiState.Capturing -> {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        this.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            if (uiState is CameraCaptureUiState.Capturing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
        is CameraCaptureUiState.Cancelled -> {
            Text(
                text = "CAPTURE CANCELLED", 
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ShutterArea(
    uiState: CameraCaptureUiState,
    onShutterClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = uiState is CameraCaptureUiState.Ready,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
            label = "ShutterButton"
        ) { isReady ->
            if (isReady) {
                ShutterButton(onClick = onShutterClick)
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
private fun CaptureStatusIndicator(uiState: CameraCaptureUiState) {
    val statusText = when (uiState) {
        is CameraCaptureUiState.Idle, is CameraCaptureUiState.Initializing -> "INITIALIZING CAMERA..."
        is CameraCaptureUiState.Ready -> "CAMERA READY"
        is CameraCaptureUiState.Capturing -> "CAPTURING..."
        is CameraCaptureUiState.Success -> "SAVED SECURELY"
        is CameraCaptureUiState.PermissionRequired -> "PERMISSION REQUIRED"
        is CameraCaptureUiState.Unavailable -> "CAMERA UNAVAILABLE"
        is CameraCaptureUiState.Failed -> "CAPTURE FAILED"
        is CameraCaptureUiState.Cancelled -> "CANCELLED"
    }
    
    val statusColor = when (uiState) {
        is CameraCaptureUiState.Ready -> MaterialTheme.colorScheme.primary
        is CameraCaptureUiState.Success -> MaterialTheme.colorScheme.tertiary
        is CameraCaptureUiState.Capturing -> MaterialTheme.colorScheme.tertiary
        is CameraCaptureUiState.Failed, is CameraCaptureUiState.PermissionRequired, is CameraCaptureUiState.Unavailable -> MaterialTheme.colorScheme.error
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
private fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .padding(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
    )
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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

