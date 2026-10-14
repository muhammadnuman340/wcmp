package com.numan.wcmp.feature.rules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.numan.wcmp.feature.rules.components.ProtectionRuleCard
import com.numan.wcmp.feature.rules.model.ProtectionRuleItem
import com.numan.wcmp.feature.rules.model.ProtectionRuleStatus
import com.numan.wcmp.ui.components.SecondaryWcmpButton
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar

@Composable
fun ProtectionRuleDetailsScreen(
    rule: ProtectionRuleItem? = null,
    onNavigateBack: () -> Unit
) {
    val displayRule = rule ?: ProtectionRuleItem(
        id = "default",
        name = "Intruder Photo Capture",
        description = "Automatically trigger front-camera photo capture whenever an unauthorized app access is detected.",
        status = ProtectionRuleStatus.NOT_IMPLEMENTED,
        trigger = "App Launch",
        target = "Protected Applications",
        action = "Front Camera Photo",
        stealthMode = true,
        retryLimit = 2
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WcmpTopBar(
                title = "RULE DETAILS",
                subtitle = "Rule specification & configuration",
                onBackClick = onNavigateBack
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                item {
                    ProtectionRuleCard(
                        name = displayRule.name,
                        description = displayRule.description,
                        status = displayRule.status,
                        trigger = displayRule.trigger,
                        target = displayRule.target,
                        action = displayRule.action,
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    WcmpCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "CONFIGURATION PARAMETERS",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "\u2022 Stealth Mode: ${if (displayRule.stealthMode) "Enabled (Silent capture)" else "Disabled"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\u2022 Encryption: AES-256-GCM hardware key",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\u2022 Retry Limit: ${displayRule.retryLimit} consecutive failed attempts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\u2022 Storage Node: Sandboxed encrypted vault",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "\u2022 Execution Engine: Standby (Preview only)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    SecondaryWcmpButton(
                        text = "BACK TO RULES",
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
