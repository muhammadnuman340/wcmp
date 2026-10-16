package com.numan.wcmp.feature.home
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.numan.wcmp.ui.components.WcmpTopBar
import com.numan.wcmp.data.WcmpRepositoryProvider

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToEvents: () -> Unit = {},
    onNavigateToProtection: () -> Unit = {},
    onNavigateToVault: () -> Unit = {},
    onNavigateToProtectedApps: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = WcmpRepositoryProvider.getSecurityRepository(context)
    val securityEngine = WcmpRepositoryProvider.getSecurityEngine(context)
    val engineState by securityEngine.state.collectAsState()
    val allEvidence by repository.observeVaultEvidence().collectAsState(initial = emptyList())
    val allEvents by repository.observeSecurityEvents().collectAsState(initial = emptyList())

    val isProtected = engineState == com.numan.wcmp.domain.security.SecurityEngineState.RUNNING
    val eventsCount = allEvents.size
    val evidenceCount = allEvidence.size
    
    val activeTime = if (isProtected && securityEngine.engineArmedAtTimestamp != null) {
        val durationMs = System.currentTimeMillis() - securityEngine.engineArmedAtTimestamp!!
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = (durationMs / (1000 * 60 * 60))
        String.format("%02dh %02dm", hours, minutes)
    } else {
        "PROTECTION INACTIVE"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        WcmpTopBar(
            onMenuClick = { /* Menu */ }
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 8.dp,
                bottom = 32.dp
            )
        ) {
            item {
                SecurityCenter(
                    isProtected = isProtected,
                    activeTime = activeTime,
                    eventsCount = eventsCount,
                    evidenceCount = evidenceCount
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                SecurityActivitySection()
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                QuickActionsSection(
                    onNavigateToEvents = onNavigateToEvents,
                    onNavigateToProtection = onNavigateToProtection,
                    onNavigateToVault = onNavigateToVault,
                    onNavigateToProtectedApps = onNavigateToProtectedApps
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                EvidenceSummarySection(
                    onNavigateToVault = onNavigateToVault
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "WCMP SECURITY ARCHITECTURE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
