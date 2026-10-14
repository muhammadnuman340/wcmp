package com.numan.wcmp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * WCMP Lightweight Navigation State
 * Clean, decoupled navigation controller suitable for Compose and future cross-platform expansion.
 */
@Stable
class WcmpNavigator(
    initialDestination: WcmpDestination = WcmpDestination.Home
) {
    var currentDestination by mutableStateOf(initialDestination)
        private set

    private val backStack = mutableStateListOf<WcmpDestination>(initialDestination)

    fun navigateTo(destination: WcmpDestination) {
        if (currentDestination != destination) {
            currentDestination = destination
            backStack.add(destination)
        }
    }

    fun popBackStack(): Boolean {
        return if (backStack.size > 1) {
            backStack.removeAt(backStack.lastIndex)
            currentDestination = backStack.last()
            true
        } else {
            false
        }
    }

    fun canNavigateBack(): Boolean = backStack.size > 1
}

@Composable
fun rememberWcmpNavigator(
    initialDestination: WcmpDestination = WcmpDestination.Home
): WcmpNavigator {
    return remember {
        WcmpNavigator(initialDestination)
    }
}
