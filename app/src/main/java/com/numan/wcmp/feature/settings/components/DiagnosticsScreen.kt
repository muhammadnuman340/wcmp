package com.numan.wcmp.feature.settings.components
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.core.permissions.AndroidCapability
import com.numan.wcmp.core.permissions.CapabilityState
import com.numan.wcmp.core.permissions.CapabilityStatus
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.EvidenceType
import com.numan.wcmp.feature.settings.diagnostics.EvidenceDiagnosticsViewModel
import com.numan.wcmp.feature.settings.diagnostics.EvidenceHealthMetrics
import com.numan.wcmp.feature.settings.diagnostics.EvidenceTestState
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar
import com.numan.wcmp.ui.icons.ChevronRight
import com.numan.wcmp.ui.icons.Delete
import com.numan.wcmp.ui.icons.ShieldAlert
import com.numan.wcmp.ui.icons.WcmpIcons

@Composable
fun DiagnosticsScreen(
    capabilities: List<CapabilityState>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coordinator = WcmpRepositoryProvider.getEvidenceCaptureCoordinator(context)
    val repository = WcmpRepositoryProvider.getSecurityRepository(context)

    val viewModel: EvidenceDiagnosticsViewModel = viewModel(
        factory = EvidenceDiagnosticsViewModel.Factory(coordinator, repository)
    )

    val testState by viewModel.testState.collectAsState()
    val healthMetrics by viewModel.healthMetrics.collectAsState()
    val isCheckingIntegrity by viewModel.isCheckingIntegrity.collectAsState()
    val integrityService = remember(context) { WcmpRepositoryProvider.getEvidenceIntegrityService(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WcmpTopBar(
            title = "Diagnostics",
            subtitle = "System health and security checks",
            onBackClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                DiagnosticsHero(
                    testState = testState,
                    onRunAll = { 
                        viewModel.runPipelineTest(EvidenceType.FRONT_CAMERA_PHOTO)
                        viewModel.runIntegrityCheck(integrityService)
                    }
                )
            }
            
            item {
                val securityEngine = com.numan.wcmp.data.WcmpRepositoryProvider.getSecurityEngine(context)
                val engineState by securityEngine.state.collectAsState()
                val isProtectionActive = engineState == com.numan.wcmp.domain.security.SecurityEngineState.RUNNING
                
                val accessibilityGranted = capabilities.find { 
                    it.capability == AndroidCapability.ACCESSIBILITY_SERVICE 
                }?.status == CapabilityStatus.GRANTED
                
                val biometricManager = androidx.biometric.BiometricManager.from(context)
                val canBiometric = biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or 
                    androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
                ) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS

                Text(
                    text = "System Health Tests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                DiagnosticRow(
                    "Protection Engine",
                    "Verify protection engine is running",
                    if (isProtectionActive) "ACTIVE" else "INACTIVE",
                    if (isProtectionActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
                DiagnosticRow(
                    "Foreground Monitoring",
                    "Check accessibility service status",
                    if (accessibilityGranted) "ACTIVE" else "REQUIRES ACCESS",
                    if (accessibilityGranted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary
                )
                DiagnosticRow(
                    "App Lock",
                    "Verify app lock functionality",
                    if (accessibilityGranted && canBiometric) "ACTIVE" else if (canBiometric) "READY" else "REQUIRES ACCESS",
                    if (accessibilityGranted && canBiometric) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                DiagnosticRow(
                    "Biometric Authentication",
                    "Check biometric authentication hardware & enrollment",
                    if (canBiometric) "READY" else "REQUIRES ACCESS",
                    if (canBiometric) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                DiagnosticRow(
                    "Evidence Encryption",
                    "Verify evidence encryption system",
                    "ACTIVE",
                    MaterialTheme.colorScheme.tertiary
                )
                DiagnosticRow(
                    "Secure Vault",
                    "Check vault integrity and access",
                    "ACTIVE",
                    MaterialTheme.colorScheme.tertiary
                )

                val integrityStatusText = if (isCheckingIntegrity) {
                    "CHECKING..."
                } else if (healthMetrics.hasRunCheck) {
                    val issues = healthMetrics.missing + healthMetrics.integrityErrors + healthMetrics.keyUnavailable
                    if (issues == 0) "HEALTHY (${healthMetrics.healthy} OK)" else "$issues ISSUES"
                } else {
                    "CHECK NOW"
                }

                val integrityStatusColor = if (isCheckingIntegrity) {
                    MaterialTheme.colorScheme.primary
                } else if (healthMetrics.hasRunCheck) {
                    val issues = healthMetrics.missing + healthMetrics.integrityErrors + healthMetrics.keyUnavailable
                    if (issues == 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }

                DiagnosticRow(
                    title = "Storage Integrity",
                    subtitle = "Verify cryptographic integrity of evidence files",
                    statusText = integrityStatusText,
                    statusColor = integrityStatusColor,
                    isLoading = isCheckingIntegrity,
                    onClick = { viewModel.runIntegrityCheck(integrityService) }
                )

                if (healthMetrics.hasRunCheck) {
                    StorageHealthBreakdownCard(
                        metrics = healthMetrics,
                        isChecking = isCheckingIntegrity,
                        onRecheck = { viewModel.runIntegrityCheck(integrityService) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Capability Tests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                @Composable
                fun getStatus(cap: AndroidCapability): String {
                    val state = capabilities.find { it.capability == cap }
                    return if (state?.status == CapabilityStatus.GRANTED) "READY" else "REQUIRES ACCESS"
                }
                
                @Composable
                fun getColor(cap: AndroidCapability): androidx.compose.ui.graphics.Color {
                    val state = capabilities.find { it.capability == cap }
                    return if (state?.status == CapabilityStatus.GRANTED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                }
                
                DiagnosticRow("Camera", "Test camera access and functionality", getStatus(AndroidCapability.CAMERA), getColor(AndroidCapability.CAMERA))
                DiagnosticRow("Microphone", "Test audio recording capability", getStatus(AndroidCapability.MICROPHONE), getColor(AndroidCapability.MICROPHONE))
                DiagnosticRow("Location", "Test location services", getStatus(AndroidCapability.LOCATION), getColor(AndroidCapability.LOCATION))
                DiagnosticRow("Notifications", "Test notification alerts", getStatus(AndroidCapability.NOTIFICATIONS), getColor(AndroidCapability.NOTIFICATIONS))
                DiagnosticRow("Accessibility", "Test foreground monitoring capability", getStatus(AndroidCapability.ACCESSIBILITY_SERVICE), getColor(AndroidCapability.ACCESSIBILITY_SERVICE))
                DiagnosticRow("Usage Access", "Test usage history access", getStatus(AndroidCapability.USAGE_ACCESS), getColor(AndroidCapability.USAGE_ACCESS))
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Evidence Pipeline Tests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                PipelineTestCard(
                    title = "Camera Pipeline Test",
                    testState = if (testState is EvidenceTestState.Running) "RUNNING" else "READY",
                    onClick = { viewModel.runPipelineTest(EvidenceType.FRONT_CAMERA_PHOTO) }
                )
                
                PipelineTestCard(
                    title = "Audio Pipeline Test",
                    testState = if (testState is EvidenceTestState.Running) "RUNNING" else "READY",
                    onClick = { viewModel.runPipelineTest(EvidenceType.AUDIO_CLIP) }
                )
                
                PipelineTestCard(
                    title = "Location Pipeline Test",
                    testState = if (testState is EvidenceTestState.Running) "RUNNING" else "READY",
                    onClick = { viewModel.runPipelineTest(EvidenceType.LOCATION_STAMP) }
                )
                
                if (testState is EvidenceTestState.Success) {
                    val state = testState as EvidenceTestState.Success
                    WcmpCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        backgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Result: SUCCESS", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.titleMedium)
                            Text(state.details, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                            Text("Evidence ID: ${state.evidenceId}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else if (testState is EvidenceTestState.Failed || testState is EvidenceTestState.Blocked) {
                    val reason = if (testState is EvidenceTestState.Failed) (testState as EvidenceTestState.Failed).reason else (testState as EvidenceTestState.Blocked).reason
                    WcmpCard(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                        backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Result: FAILED", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                            Text(reason, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                WcmpCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentPadding = 16.dp,
                    backgroundColor = MaterialTheme.colorScheme.surface
                ) {
                    Column {
                        Text(
                            text = "Test Evidence Cleanup",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delete simulated test records created during diagnostics. Real incident vault evidence is preserved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.clearTestEvidence() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = WcmpIcons.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "CLEAR TEST EVIDENCE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsHero(testState: EvidenceTestState, onRunAll: () -> Unit) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentPadding = 20.dp,
        backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = WcmpIcons.ShieldAlert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "SYSTEM DIAGNOSTICS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Run tests to verify your security setup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Button(
                onClick = onRunAll,
                enabled = testState !is EvidenceTestState.Running,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (testState is EvidenceTestState.Running) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "RUN TEST",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticRow(
    title: String,
    subtitle: String,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    isLoading: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentPadding = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WcmpIcons.ShieldAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(10.dp),
                        strokeWidth = 1.5.dp,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else {
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = statusColor
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            if (onClick != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = WcmpIcons.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun StorageHealthBreakdownCard(
    metrics: EvidenceHealthMetrics,
    isChecking: Boolean,
    onRecheck: () -> Unit
) {
    val hasIssues = metrics.missing > 0 || metrics.integrityErrors > 0 || metrics.keyUnavailable > 0
    val cardBorder = if (hasIssues) MaterialTheme.colorScheme.error.copy(alpha = 0.35f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)

    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentPadding = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        borderColor = cardBorder
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STORAGE INTEGRITY REPORT",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("Healthy", metrics.healthy.toString(), MaterialTheme.colorScheme.tertiary)
                MetricColumn("Missing", metrics.missing.toString(), if (metrics.missing > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                MetricColumn("Corrupted", metrics.integrityErrors.toString(), if (metrics.integrityErrors > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricColumn("Key Unavail", metrics.keyUnavailable.toString(), if (metrics.keyUnavailable > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                MetricColumn("Orphans", metrics.orphans.toString(), if (metrics.orphans > 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant)
                MetricColumn("Legacy", metrics.legacy.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun PipelineTestCard(
    title: String,
    testState: String,
    onClick: () -> Unit
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        contentPadding = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (testState == "RUNNING") "RUNNING..." else "RUN TEST")
            }
        }
    }
}
