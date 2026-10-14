package com.numan.wcmp.feature.rules

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.numan.wcmp.feature.rules.components.ProtectionRuleCard
import com.numan.wcmp.feature.rules.components.RulesEmptyState
import com.numan.wcmp.feature.rules.components.RulesFilterBar
import com.numan.wcmp.feature.rules.components.RulesSearchBox
import com.numan.wcmp.feature.rules.components.RulesSecurityHero
import com.numan.wcmp.feature.rules.components.RulesSummarySection
import com.numan.wcmp.feature.rules.components.SmartProtectionCard
import com.numan.wcmp.feature.rules.model.ProtectionRuleItem
import com.numan.wcmp.feature.rules.model.ProtectionRuleStatus
import com.numan.wcmp.ui.components.PrimaryWcmpButton
import com.numan.wcmp.ui.components.WcmpTopBar

private enum class RulesSubView {
    LIST,
    CREATE,
    DETAILS
}

@Composable
fun RulesScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    onBackClick: (() -> Unit)? = null
) {
    var subView by remember { mutableStateOf(RulesSubView.LIST) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedRule by remember { mutableStateOf<ProtectionRuleItem?>(null) }

    val templateRules = remember {
        listOf(
            ProtectionRuleItem(
                id = "rule_1",
                name = "Intruder Photo Capture",
                description = "Automatically triggers front camera photo capture whenever a protected application is launched without authorization.",
                status = ProtectionRuleStatus.ACTIVE,
                trigger = "Protected App Launched",
                target = "App Lock Protected Apps",
                action = "Front Camera Photo",
                stealthMode = true,
                retryLimit = 2
            ),
            ProtectionRuleItem(
                id = "rule_2",
                name = "Failed Biometric Audio Log",
                description = "Silently records a 10-second ambient audio clip after two consecutive failed biometric or PIN unlock attempts.",
                status = ProtectionRuleStatus.NOT_IMPLEMENTED,
                trigger = "Failed Biometric Auth (2x)",
                target = "Lock Screen / Vault",
                action = "10s Silent Audio Clip",
                stealthMode = true,
                retryLimit = 2
            ),
            ProtectionRuleItem(
                id = "rule_3",
                name = "After-Hours Location Stamp",
                description = "Stamps current device GPS coordinates whenever device access occurs outside designated safe hours (10 PM - 6 AM).",
                status = ProtectionRuleStatus.DISABLED,
                trigger = "Access (10 PM - 6 AM)",
                target = "Device Foreground",
                action = "Log GPS Location Stamp",
                stealthMode = false,
                retryLimit = 1
            )
        )
    }

    val filteredRules = templateRules.filter { rule ->
        val matchesSearch = rule.name.contains(searchQuery, ignoreCase = true) ||
                rule.description.contains(searchQuery, ignoreCase = true) ||
                rule.trigger.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "ALL" -> true
            "ACTIVE" -> rule.status == ProtectionRuleStatus.ACTIVE
            "DISABLED" -> rule.status == ProtectionRuleStatus.DISABLED
            "NOT IMPLEMENTED" -> rule.status == ProtectionRuleStatus.NOT_IMPLEMENTED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    AnimatedContent(
        targetState = subView,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
        },
        label = "RulesSubViewTransition"
    ) { targetView ->
        when (targetView) {
            RulesSubView.CREATE -> {
                CreateProtectionRuleScreen(onNavigateBack = { subView = RulesSubView.LIST })
            }
            RulesSubView.DETAILS -> {
                ProtectionRuleDetailsScreen(
                    rule = selectedRule,
                    onNavigateBack = { subView = RulesSubView.LIST }
                )
            }
            RulesSubView.LIST -> {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        WcmpTopBar(
                            title = "PROTECTION RULES",
                            subtitle = "Automate your security with smart rules",
                            onMenuClick = onMenuClick,
                            onBackClick = onBackClick
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            item {
                                RulesSecurityHero(ruleCount = templateRules.size)
                            }

                            item {
                                SmartProtectionCard()
                            }

                            item {
                                RulesSummarySection(
                                    totalRules = templateRules.size,
                                    activeRules = templateRules.count { it.status == ProtectionRuleStatus.ACTIVE },
                                    availableTriggers = 4
                                )
                            }

                            item {
                                RulesSearchBox(
                                    query = searchQuery,
                                    onQueryChanged = { searchQuery = it }
                                )
                            }

                            item {
                                RulesFilterBar(
                                    selectedFilter = selectedFilter,
                                    onFilterSelected = { selectedFilter = it }
                                )
                            }

                            if (filteredRules.isEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    RulesEmptyState(onCreateClick = { subView = RulesSubView.CREATE })
                                }
                            } else {
                                items(filteredRules) { rule ->
                                    ProtectionRuleCard(
                                        name = rule.name,
                                        description = rule.description,
                                        status = rule.status,
                                        trigger = rule.trigger,
                                        target = rule.target,
                                        action = rule.action,
                                        onClick = {
                                            selectedRule = rule
                                            subView = RulesSubView.DETAILS
                                        }
                                    )
                                }

                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        PrimaryWcmpButton(
                                            text = "+ CREATE NEW RULE",
                                            onClick = { subView = RulesSubView.CREATE },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
