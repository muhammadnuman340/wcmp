package com.numan.wcmp.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.numan.wcmp.ui.icons.WcmpIcons

/**
 * WCMP Navigation Destinations
 * Clean architecture destinations decoupled from specific framework routers.
 */
sealed class WcmpDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Home : WcmpDestination(
        route = "home",
        title = "Dashboard",
        icon = WcmpIcons.ShieldCheck
    )

    data object Events : WcmpDestination(
        route = "events",
        title = "Log / Events",
        icon = WcmpIcons.History
    )

    data object Vault : WcmpDestination(
        route = "vault",
        title = "Evidence",
        icon = WcmpIcons.Lock
    )

    data object Protection : WcmpDestination(
        route = "protection",
        title = "Protection",
        icon = WcmpIcons.Radar
    )

    data object ProtectedApps : WcmpDestination(
        route = "protected_apps",
        title = "App Lock",
        icon = WcmpIcons.Apps
    )

    data object Rules : WcmpDestination(
        route = "rules",
        title = "Rules",
        icon = WcmpIcons.Eye
    )

    data object Settings : WcmpDestination(
        route = "settings",
        title = "Settings",
        icon = WcmpIcons.Settings
    )

    companion object {
        val bottomNavDestinations: List<WcmpDestination>
            get() = listOf(
                Home,
                Events,
                Vault,
                Protection,
                Settings
            )
    }
}
