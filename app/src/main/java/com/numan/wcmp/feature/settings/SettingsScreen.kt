package com.numan.wcmp.feature.settings
import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.numan.wcmp.ui.animation.SecureTransition
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.core.permissions.AndroidCapability
import com.numan.wcmp.core.permissions.CapabilityState
import com.numan.wcmp.core.permissions.CapabilityStatus
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.feature.settings.components.DiagnosticsScreen
import com.numan.wcmp.feature.settings.viewmodel.SettingsViewModel
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar

import com.numan.wcmp.ui.icons.ChevronRight
import com.numan.wcmp.ui.icons.LocationOn
import com.numan.wcmp.ui.icons.Mic
import com.numan.wcmp.ui.icons.ShieldAlert
import com.numan.wcmp.ui.icons.WcmpIcons

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            securityRepository = WcmpRepositoryProvider.getSecurityRepository(LocalContext.current),
            capabilityRepository = WcmpRepositoryProvider.getCapabilityRepository(LocalContext.current)
        )
    )
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val capabilities by viewModel.capabilities.collectAsState()
    
    var showDiagnostics by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WcmpTopBar(
                title = "SETTINGS",
                subtitle = "Security control center",
                onMenuClick = onMenuClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item {
                    SettingsSecurityHealthHero(capabilities = capabilities)
                }
                
                item {
                    ProtectionSettingsCard(
                        protectionMode = settingsState.protectionMode,
                        autoProtectionEnabled = settingsState.autoProtectionEnabled,
                        onAutoProtectionChanged = { viewModel.setAutoProtection(it) }
                    )
                }

                item {
                    DetectionSettingsCard()
                }

                item {
                    EvidenceSettingsCard()
                }

                item {
                    NotificationSettingsCard(
                        notificationsEnabled = settingsState.notificationAlertsEnabled,
                        onNotificationsChanged = { viewModel.setNotificationAlerts(it) },
                        capabilities = capabilities
                    )
                }

                item {
                    PrivacySecurityCard()
                }

                item {
                    AppearanceSettingsCard(
                        animationsEnabled = settingsState.animationsEnabled,
                        onAnimationsChanged = { viewModel.setAnimations(it) }
                    )
                }

                item {
                    DiagnosticsSettingsCard(
                        onClick = { showDiagnostics = true }
                    )
                }

                item {
                    AboutWcmpCard()
                }
            }
        }

        AnimatedVisibility(
            visible = showDiagnostics,
            enter = SecureTransition.enterFadeScale,
            exit = SecureTransition.exitFadeScale,
            modifier = Modifier.fillMaxSize()
        ) {
            DiagnosticsScreen(
                capabilities = capabilities,
                onBack = { showDiagnostics = false }
            )
        }
    }
}

@Composable
private fun SettingsSecurityHealthHero(capabilities: List<CapabilityState>) {
    // Determine overall health based on capabilities
    val (status, description, icon, color) = if (capabilities.isEmpty()) {
        listOf("NOT CONNECTED", "Capabilities are initializing", WcmpIcons.ShieldAlert, MaterialTheme.colorScheme.onSurfaceVariant)
    } else if (capabilities.any { it.status == CapabilityStatus.REQUIRES_SPECIAL_ACCESS || it.status == CapabilityStatus.NOT_REQUESTED }) {
        listOf("REQUIRES ACCESS", "One or more required systems need authorization", WcmpIcons.ShieldAlert, MaterialTheme.colorScheme.error)
    } else if (capabilities.any { it.status == CapabilityStatus.NOT_IMPLEMENTED }) {
        listOf("LIMITED", "Some capabilities are restricted", WcmpIcons.ShieldAlert, MaterialTheme.colorScheme.error)
    } else {
        listOf("READY", "All core systems are operational", WcmpIcons.ShieldCheck, MaterialTheme.colorScheme.tertiary)
    }
    
    val healthyCount = capabilities.count { it.status == CapabilityStatus.GRANTED }
    val limitedCount = capabilities.count { it.status == CapabilityStatus.NOT_IMPLEMENTED }
    val errorCount = capabilities.count { it.status == CapabilityStatus.REQUIRES_SPECIAL_ACCESS || it.status == CapabilityStatus.NOT_REQUESTED }

    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentPadding = 20.dp,
        backgroundColor = (color as androidx.compose.ui.graphics.Color).copy(alpha = 0.05f),
        borderColor = (color as androidx.compose.ui.graphics.Color).copy(alpha = 0.2f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Shield Ring
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "SECURITY HEALTH",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = status as String,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                    Text(
                        text = description as String,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusPill(WcmpIcons.Check, "$healthyCount Healthy", MaterialTheme.colorScheme.tertiary)
                StatusPill(WcmpIcons.ShieldAlert, "$limitedCount Limited", MaterialTheme.colorScheme.secondary)
                StatusPill(WcmpIcons.ShieldAlert, "$errorCount Errors", MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun StatusPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentPadding = 16.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
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
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                        color = statusColor
                    )
                }
                
                if (onClick != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = WcmpIcons.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            if (content != null) {
                Spacer(modifier = Modifier.height(16.dp))
                content()
            }
        }
    }
}

@Composable
private fun SettingsRowSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsRowStatus(
    title: String,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = statusColor
        )
    }
}

@Composable
private fun ProtectionSettingsCard(
    protectionMode: String,
    autoProtectionEnabled: Boolean,
    onAutoProtectionChanged: (Boolean) -> Unit
) {
    SettingsCard(
        title = "Protection",
        subtitle = "Manage protection mode and app access control settings.",
        icon = WcmpIcons.ShieldCheck,
        statusText = "ACTIVE",
        statusColor = MaterialTheme.colorScheme.tertiary,
        onClick = null
    ) {
        SettingsRowStatus("Current Mode", protectionMode, MaterialTheme.colorScheme.tertiary)
        SettingsRowStatus("Foreground Monitoring", "READY", MaterialTheme.colorScheme.tertiary)
        SettingsRowStatus("App Lock", "READY", MaterialTheme.colorScheme.tertiary)
        SettingsRowSwitch("Auto Protection", autoProtectionEnabled, onAutoProtectionChanged)
    }
}

@Composable
private fun DetectionSettingsCard() {
    SettingsCard(
        title = "Detection",
        subtitle = "Configure monitoring and detection capabilities.",
        icon = WcmpIcons.LocationOn, // Placeholder icon
        statusText = "READY",
        statusColor = MaterialTheme.colorScheme.primary,
        onClick = null
    ) {
        SettingsRowStatus("Motion Detection", "NOT IMPLEMENTED")
        SettingsRowStatus("Smart Protection", "NOT IMPLEMENTED")
        SettingsRowStatus("Scheduled Protection", "NOT IMPLEMENTED")
    }
}

@Composable
private fun EvidenceSettingsCard() {
    SettingsCard(
        title = "Evidence & Vault",
        subtitle = "Manage evidence capture and secure storage.",
        icon = WcmpIcons.ShieldCheck, // Placeholder
        statusText = "ENCRYPTED",
        statusColor = MaterialTheme.colorScheme.tertiary,
        onClick = null
    ) {
        SettingsRowStatus("Evidence Encryption", "ACTIVE", MaterialTheme.colorScheme.tertiary)
        SettingsRowStatus("Storage Method", "AES-GCM • Android Keystore", MaterialTheme.colorScheme.onSurfaceVariant)
        SettingsRowStatus("Secure Vault", "READY", MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun NotificationSettingsCard(
    notificationsEnabled: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    capabilities: List<CapabilityState>
) {
    val notifCapability = capabilities.find { it.capability == AndroidCapability.NOTIFICATIONS }
    val status = if (notifCapability?.status == CapabilityStatus.GRANTED) "READY" else "REQUIRES ACCESS"
    val color = if (status == "READY") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    
    SettingsCard(
        title = "Notifications",
        subtitle = "Configure security notifications and alerts.",
        icon = WcmpIcons.ShieldAlert, // Placeholder
        statusText = status,
        statusColor = color,
        onClick = null
    ) {
        SettingsRowSwitch("Notification Alerts", notificationsEnabled, onNotificationsChanged)
    }
}

@Composable
private fun PrivacySecurityCard() {
    SettingsCard(
        title = "Privacy & Security",
        subtitle = "Privacy controls and security settings.",
        icon = WcmpIcons.ShieldCheck, // Placeholder
        statusText = "READY",
        statusColor = MaterialTheme.colorScheme.primary,
        onClick = null
    ) {
        SettingsRowStatus("Local-first storage", "ACTIVE", MaterialTheme.colorScheme.tertiary)
        SettingsRowStatus("Cloud evidence upload", "DISABLED (No Cloud)", MaterialTheme.colorScheme.tertiary)
        SettingsRowStatus("Analytics/Tracking", "DISABLED", MaterialTheme.colorScheme.tertiary)
        SettingsRowStatus("App-private storage", "ACTIVE", MaterialTheme.colorScheme.tertiary)
    }
}

@Composable
private fun AppearanceSettingsCard(
    animationsEnabled: Boolean,
    onAnimationsChanged: (Boolean) -> Unit
) {
    SettingsCard(
        title = "Appearance",
        subtitle = "Theme, display and visual settings.",
        icon = WcmpIcons.Eye, // Placeholder
        statusText = "READY",
        statusColor = MaterialTheme.colorScheme.primary,
        onClick = null
    ) {
        SettingsRowStatus("Theme", "Premium V2 Dark")
        SettingsRowSwitch("Enable Animations", animationsEnabled, onAnimationsChanged)
    }
}

@Composable
private fun DiagnosticsSettingsCard(onClick: () -> Unit) {
    SettingsCard(
        title = "Diagnostics",
        subtitle = "Run system checks and view detailed reports.",
        icon = WcmpIcons.Check, // Placeholder for stethoscope/medical
        statusText = "AVAILABLE",
        statusColor = MaterialTheme.colorScheme.tertiary,
        onClick = onClick
    )
}

@Composable
private fun AboutWcmpCard() {
    SettingsCard(
        title = "About WCMP",
        subtitle = "App information and version details.",
        icon = WcmpIcons.ShieldCheck, // Placeholder for info
        statusText = "READY",
        statusColor = MaterialTheme.colorScheme.primary,
        onClick = null
    ) {
        SettingsRowStatus("Name", "WCMP")
        SettingsRowStatus("Description", "Who Checked My Phone")
        SettingsRowStatus("Version", "1.0.0 (Debug)")
        SettingsRowStatus("Architecture", "Local-first Security Engine")
    }
}
