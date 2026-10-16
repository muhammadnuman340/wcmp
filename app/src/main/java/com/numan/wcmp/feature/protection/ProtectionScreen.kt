package com.numan.wcmp.feature.protection
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.core.permissions.AndroidCapability
import com.numan.wcmp.core.permissions.CapabilityStatus
import com.numan.wcmp.domain.security.SecurityEngineState
import com.numan.wcmp.ui.components.DangerWcmpButton
import com.numan.wcmp.ui.components.PrimaryWcmpButton
import com.numan.wcmp.ui.components.SecondaryWcmpButton
import com.numan.wcmp.ui.components.SecurityRing
import com.numan.wcmp.ui.components.SecurityRingState
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar
import com.numan.wcmp.ui.icons.WcmpIcons

@Composable
fun ProtectionScreen(
    modifier: Modifier = Modifier,
    onNavigateToProtectedApps: () -> Unit = {},
    onNavigateToRules: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    viewModel: ProtectionViewModel = viewModel(
        factory = ProtectionViewModel.Factory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    var showStopConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshCapabilities()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WcmpTopBar(
            title = "Protection",
            subtitle = "Security control center",
            onMenuClick = { /* TODO */ }
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
        ) {
            // Protection Hero
            item {
                ProtectionHero(
                    uiState = uiState,
                    onActionClick = {
                        when (uiState.engineState) {
                            SecurityEngineState.RUNNING, SecurityEngineState.STARTING -> {
                                showStopConfirmation = true
                            }
                            SecurityEngineState.DISABLED -> {
                                viewModel.startProtection()
                            }
                            SecurityEngineState.WAITING_FOR_ACCESS, SecurityEngineState.ERROR -> {
                                viewModel.startProtection()
                            }
                        }
                    }
                )
            }

            // Access Recovery
            if (uiState.engineState == SecurityEngineState.WAITING_FOR_ACCESS) {
                item {
                    ActionRequiredCard(
                        title = "Accessibility access required",
                        subtitle = "WCMP needs Accessibility permission to monitor foreground apps and protect them.",
                        actionText = "FIX ACCESS",
                        onAction = { viewModel.startProtection() }
                    )
                }
            } else if (uiState.getCapabilityState(AndroidCapability.CAMERA)?.status != CapabilityStatus.GRANTED) {
                item {
                    ActionRequiredCard(
                        title = "Camera permission required",
                        subtitle = "WCMP needs Camera permission to capture intruder photos.",
                        actionText = "GRANT",
                        onAction = { viewModel.refreshCapabilities() }
                    )
                }
            }

            // Active Session Information
            if (uiState.activeSession != null) {
                item {
                    ActiveSessionCard(session = uiState.activeSession!!)
                }
            }

            // Protection Mode
            item {
                ProtectionModeSection(
                    selectedMode = uiState.selectedMode,
                    onModeSelected = { viewModel.setMode(it) }
                )
            }

            // Detection Configuration
            item {
                ConfigurationSection(
                    title = "DETECTION",
                    items = listOf(
                        ConfigItemUI(
                            name = "Protected Apps",
                            description = "${uiState.protectedAppCount} protected apps",
                            stateLabel = "READY",
                            stateColor = MaterialTheme.colorScheme.tertiary
                        ),
                        ConfigItemUI(
                            name = "Foreground Monitoring",
                            description = "Observes protected app foreground access",
                            stateLabel = when(uiState.engineState) {
                                SecurityEngineState.RUNNING -> "ACTIVE"
                                SecurityEngineState.WAITING_FOR_ACCESS -> "REQUIRES ACCESS"
                                SecurityEngineState.ERROR -> "ERROR"
                                else -> "SERVICE ENABLED"
                            },
                            stateColor = when(uiState.engineState) {
                                SecurityEngineState.RUNNING -> MaterialTheme.colorScheme.tertiary
                                SecurityEngineState.WAITING_FOR_ACCESS -> MaterialTheme.colorScheme.error
                                SecurityEngineState.ERROR -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ),
                        ConfigItemUI(
                            name = "Motion Detection",
                            description = "Detect device movement when armed",
                            stateLabel = "NOT IMPLEMENTED",
                            stateColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                )
            }

            // Evidence Configuration
            item {
                ConfigurationSection(
                    title = "EVIDENCE",
                    items = listOf(
                        ConfigItemUI(
                            name = "Camera",
                            description = "Foreground evidence capture",
                            stateLabel = if (uiState.getCapabilityState(AndroidCapability.CAMERA)?.status == CapabilityStatus.GRANTED) "READY" else "REQUIRES ACCESS",
                            stateColor = if (uiState.getCapabilityState(AndroidCapability.CAMERA)?.status == CapabilityStatus.GRANTED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        ),
                        ConfigItemUI(
                            name = "Audio",
                            description = "Foreground recording",
                            stateLabel = if (uiState.getCapabilityState(AndroidCapability.MICROPHONE)?.status == CapabilityStatus.GRANTED) "READY" else "REQUIRES ACCESS",
                            stateColor = if (uiState.getCapabilityState(AndroidCapability.MICROPHONE)?.status == CapabilityStatus.GRANTED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        ),
                        ConfigItemUI(
                            name = "Location",
                            description = "One-shot location evidence",
                            stateLabel = if (uiState.getCapabilityState(AndroidCapability.LOCATION)?.status == CapabilityStatus.GRANTED) "READY" else "REQUIRES ACCESS",
                            stateColor = if (uiState.getCapabilityState(AndroidCapability.LOCATION)?.status == CapabilityStatus.GRANTED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                        )
                    )
                )
            }

            // Security Health
            item {
                SecurityHealthSection(uiState = uiState)
            }

            // Quick Access
            item {
                SectionHeader("QUICK ACCESS")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "Protected Apps",
                        icon = WcmpIcons.Apps,
                        onClick = onNavigateToProtectedApps
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "Rules",
                        icon = WcmpIcons.Eye,
                        onClick = onNavigateToRules
                    )
                    QuickAccessCard(
                        modifier = Modifier.weight(1f),
                        title = "Secure Vault",
                        icon = WcmpIcons.ShieldCheck,
                        onClick = onNavigateToVault
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showStopConfirmation) {
        StopProtectionDialog(
            onDismiss = { showStopConfirmation = false },
            onConfirm = {
                viewModel.stopProtection()
                showStopConfirmation = false
            }
        )
    }
}

@Composable
private fun ProtectionHero(
    uiState: ProtectionUiState,
    onActionClick: () -> Unit
) {
    val ringState = when (uiState.engineState) {
        SecurityEngineState.RUNNING -> SecurityRingState.ACTIVE
        SecurityEngineState.STARTING -> SecurityRingState.ACTIVE
        SecurityEngineState.WAITING_FOR_ACCESS -> SecurityRingState.ERROR
        SecurityEngineState.ERROR -> SecurityRingState.ERROR
        SecurityEngineState.DISABLED -> SecurityRingState.STOPPED
    }
    
    val statusText = when (uiState.engineState) {
        SecurityEngineState.RUNNING -> "PROTECTED"
        SecurityEngineState.STARTING -> "STARTING"
        SecurityEngineState.WAITING_FOR_ACCESS -> "REQUIRES ACCESS"
        SecurityEngineState.ERROR -> "ERROR"
        SecurityEngineState.DISABLED -> "STOPPED"
    }
    
    val statusColor = when (uiState.engineState) {
        SecurityEngineState.RUNNING -> MaterialTheme.colorScheme.tertiary
        SecurityEngineState.WAITING_FOR_ACCESS -> MaterialTheme.colorScheme.error
        SecurityEngineState.ERROR -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    val actionText = when (uiState.engineState) {
        SecurityEngineState.RUNNING, SecurityEngineState.STARTING -> "STOP PROTECTION"
        SecurityEngineState.WAITING_FOR_ACCESS -> "FIX ACCESS"
        SecurityEngineState.ERROR -> "VIEW REQUIREMENTS"
        SecurityEngineState.DISABLED -> "START PROTECTION"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            SecurityRing(state = ringState, size = 160.dp)
            Icon(
                imageVector = WcmpIcons.ShieldCheck,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = statusColor
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "PROTECTION STATUS",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = statusColor
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (uiState.engineState == SecurityEngineState.RUNNING || uiState.engineState == SecurityEngineState.STARTING) {
            SecondaryWcmpButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
            )
        } else {
            PrimaryWcmpButton(
                text = actionText,
                onClick = onActionClick,
                modifier = Modifier.padding(horizontal = 48.dp).fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ActiveSessionCard(session: com.numan.wcmp.domain.security.SecuritySession) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        SectionHeader("ACTIVE PROTECTION SESSION")
        WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "PROTECTED FOR",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(System.currentTimeMillis() - session.startedAt) / 60000}m",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EVENTS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = session.eventCount.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EVIDENCE",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = session.evidenceCount.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtectionModeSection(
    selectedMode: ProtectionMode,
    onModeSelected: (ProtectionMode) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        SectionHeader("PROTECTION MODE", modifier = Modifier.padding(horizontal = 20.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
        ) {
            val modes = ProtectionMode.values()
            items(modes) { mode ->
                val isSelected = mode == selectedMode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { onModeSelected(mode) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.replace("_", " ").lowercase().split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class ConfigItemUI(val name: String, val description: String, val stateLabel: String, val stateColor: Color)

@Composable
private fun ConfigurationSection(title: String, items: List<ConfigItemUI>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        SectionHeader(title)
        WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 0.dp) {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = item.stateLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = item.stateColor
                    )
                }
                if (index < items.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun SecurityHealthSection(uiState: ProtectionUiState) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        SectionHeader("SECURITY HEALTH")
        WcmpCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                HealthItem("Protection", if (uiState.engineState == SecurityEngineState.RUNNING) "ACTIVE" else "READY", if (uiState.engineState == SecurityEngineState.RUNNING) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                HealthItem("Monitoring", if (uiState.engineState == SecurityEngineState.WAITING_FOR_ACCESS) "LIMITED" else "READY", if (uiState.engineState == SecurityEngineState.WAITING_FOR_ACCESS) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary)
                HealthItem("Evidence", if (uiState.getCapabilityState(AndroidCapability.CAMERA)?.status == CapabilityStatus.GRANTED) "READY" else "LIMITED", if (uiState.getCapabilityState(AndroidCapability.CAMERA)?.status == CapabilityStatus.GRANTED) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun HealthItem(label: String, status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionRequiredCard(
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentPadding = 16.dp,
        backgroundColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = WcmpIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ACTION REQUIRED",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryWcmpButton(
            text = actionText,
            onClick = onAction,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun QuickAccessCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    WcmpCard(
        modifier = modifier.clickable { onClick() },
        contentPadding = 16.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 1.5.sp,
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StopProtectionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Stop Protection?",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "WCMP will stop active protection monitoring. Your device will not be protected against unauthorized access.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            DangerWcmpButton(
                text = "STOP PROTECTION",
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
