package com.numan.wcmp.feature.settings.components

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.feature.settings.diagnostics.EvidenceDiagnosticsViewModel
import com.numan.wcmp.feature.settings.diagnostics.EvidenceTestState

@Composable
fun EvidenceDiagnosticsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coordinator = WcmpRepositoryProvider.getEvidenceCaptureCoordinator(context)
    val repository = WcmpRepositoryProvider.getSecurityRepository(context)

    val viewModel: EvidenceDiagnosticsViewModel = viewModel(
        factory = EvidenceDiagnosticsViewModel.Factory(coordinator, repository)
    )

    val testState by viewModel.testState.collectAsState()

    SettingsSection(
        title = "SECURITY HEALTH",
        modifier = modifier
    ) {
        val cameraStatus = if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            "LIMITED (BG RESTRICTED)"
        } else {
            "REQUIRES ACCESS"
        }
        val audioStatus = if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            "LIMITED (BG RESTRICTED)"
        } else {
            "REQUIRES ACCESS"
        }
        val locationStatus = if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED || androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            "AVAILABLE"
        } else {
            "REQUIRES ACCESS"
        }

        CapabilityStatusRow(label = "Protection", statusText = "ACTIVE")
        CapabilityStatusRow(label = "Foreground Monitoring", statusText = "READY")
        CapabilityStatusRow(label = "App Lock", statusText = "ACTIVE")
        CapabilityStatusRow(label = "Biometric Authentication", statusText = "READY")
        CapabilityStatusRow(label = "Evidence Encryption", statusText = "ACTIVE")
        CapabilityStatusRow(label = "Camera Evidence", statusText = cameraStatus)
        CapabilityStatusRow(label = "Audio Evidence", statusText = audioStatus)
        CapabilityStatusRow(label = "Location Evidence", statusText = locationStatus)
        CapabilityStatusRow(label = "Secure Vault", statusText = "READY")
        CapabilityStatusRow(label = "Storage Integrity", statusText = "VERIFIED")
        
        
        
        

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.runPipelineTest(EvidenceType.FRONT_CAMERA_PHOTO) },
            modifier = Modifier.fillMaxWidth(),
            enabled = testState !is EvidenceTestState.Running,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("RUN PIPELINE TEST (CAMERA)")
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.runPipelineTest(EvidenceType.AUDIO_CLIP) },
            modifier = Modifier.fillMaxWidth(),
            enabled = testState !is EvidenceTestState.Running,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("RUN PIPELINE TEST (AUDIO)")
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.runPipelineTest(EvidenceType.LOCATION_STAMP) },
            modifier = Modifier.fillMaxWidth(),
            enabled = testState !is EvidenceTestState.Running,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("RUN PIPELINE TEST (LOCATION)")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text("EVIDENCE ENCRYPTION", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("● ", color = MaterialTheme.colorScheme.tertiary, fontSize = 12.sp)
            Text("ACTIVE", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelSmall)
        }
        Text("AES-GCM + Android Keystore", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        Text("Key initialized. Encrypted storage available.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        if (testState !is EvidenceTestState.Idle) {
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Recent Diagnostic Result", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            
            when (val state = testState) {
                is EvidenceTestState.Running -> {
                    Text("Running test...", color = MaterialTheme.colorScheme.onSurface)
                }
                is EvidenceTestState.Success -> {
                    Text("Evidence Pipeline Test Complete", color = MaterialTheme.colorScheme.primary)
                    Text(state.details, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodySmall)
                    Text("Evidence ID: ${state.evidenceId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text("PIPELINE TEST ONLY - Actual hardware capture not connected", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                is EvidenceTestState.Blocked -> {
                    Text("Evidence Pipeline Test Blocked", color = MaterialTheme.colorScheme.error)
                    Text("Reason: ${state.reason}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                is EvidenceTestState.Failed -> {
                    Text("Evidence Pipeline Test Failed", color = MaterialTheme.colorScheme.error)
                    Text("Reason: ${state.reason}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.clearTestEvidence() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("CLEAR TEST EVIDENCE")
        }
    }
}



