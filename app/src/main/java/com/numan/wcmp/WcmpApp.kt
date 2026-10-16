package com.numan.wcmp

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.numan.wcmp.core.navigation.WcmpDestination
import com.numan.wcmp.core.navigation.WcmpNavigator
import com.numan.wcmp.core.navigation.rememberWcmpNavigator
import com.numan.wcmp.feature.events.EventsScreen
import com.numan.wcmp.feature.home.HomeScreen
import com.numan.wcmp.feature.protectedapps.ProtectedAppsScreen
import com.numan.wcmp.feature.protection.ProtectionScreen
import com.numan.wcmp.feature.rules.RulesScreen
import com.numan.wcmp.feature.settings.SettingsScreen
import com.numan.wcmp.feature.vault.VaultScreen
import com.numan.wcmp.ui.theme.WCMPTheme

@Composable
fun WcmpApp(
    darkTheme: Boolean = isSystemInDarkTheme(),
    navigator: WcmpNavigator = rememberWcmpNavigator()
) {
    BackHandler(enabled = navigator.canNavigateBack()) {
        navigator.popBackStack()
    }

    WCMPTheme(darkTheme = darkTheme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                WcmpBottomNavigation(
                    currentDestination = navigator.currentDestination,
                    onDestinationSelected = { destination ->
                        navigator.navigateTo(destination)
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = navigator.currentDestination,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(220)) togetherWith
                            fadeOut(animationSpec = tween(180))
                    },
                    label = "DestinationTransition",
                    modifier = Modifier.fillMaxSize()
                ) { destination ->
                    when (destination) {
                        WcmpDestination.Home -> HomeScreen(
                            onNavigateToEvents = { navigator.navigateTo(WcmpDestination.Events) },
                            onNavigateToProtection = { navigator.navigateTo(WcmpDestination.Protection) },
                            onNavigateToVault = { navigator.navigateTo(WcmpDestination.Vault) },
                            onNavigateToProtectedApps = { navigator.navigateTo(WcmpDestination.ProtectedApps) }
                        )
                        WcmpDestination.Events -> EventsScreen()
                        WcmpDestination.Vault -> VaultScreen()
                        WcmpDestination.Protection -> ProtectionScreen(
                            onNavigateToProtectedApps = {
                                navigator.navigateTo(WcmpDestination.ProtectedApps)
                            },
                            onNavigateToRules = {
                                navigator.navigateTo(WcmpDestination.Rules)
                            },
                            onNavigateToVault = {
                                navigator.navigateTo(WcmpDestination.Vault)
                            }
                        )
                        WcmpDestination.ProtectedApps -> ProtectedAppsScreen(
                            onNavigateBack = {
                                if (!navigator.popBackStack()) {
                                    navigator.navigateTo(WcmpDestination.Protection)
                                }
                            }
                        )
                        WcmpDestination.Rules -> RulesScreen(
                            onBackClick = {
                                if (!navigator.popBackStack()) {
                                    navigator.navigateTo(WcmpDestination.Protection)
                                }
                            }
                        )
                        WcmpDestination.Settings -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun WcmpBottomNavigation(
    currentDestination: WcmpDestination,
    onDestinationSelected: (WcmpDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        WcmpDestination.bottomNavDestinations.forEach { destination ->
            val isSelected = currentDestination == destination ||
                (destination == WcmpDestination.Protection &&
                    (currentDestination == WcmpDestination.ProtectedApps ||
                        currentDestination == WcmpDestination.Rules))

            NavigationBarItem(
                selected = isSelected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.title
                    )
                },
                label = {
                    Text(
                        text = destination.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.background,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
