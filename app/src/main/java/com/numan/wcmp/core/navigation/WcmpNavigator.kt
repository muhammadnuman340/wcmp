package com.numan.wcmp.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class WcmpNavigator(
    initialDestination: WcmpDestination = WcmpDestination.Home,
    initialBackStack: List<WcmpDestination> = listOf(initialDestination)
) {
    private val backStack = mutableStateListOf<WcmpDestination>().apply {
        addAll(initialBackStack.ifEmpty { listOf(initialDestination) })
    }

    var currentDestination by mutableStateOf(backStack.last())
        private set

    fun navigateTo(destination: WcmpDestination) {
        if (currentDestination == destination) return

        if (destination in WcmpDestination.bottomNavDestinations) {
            backStack.clear()
            if (destination != WcmpDestination.Home) {
                backStack.add(WcmpDestination.Home)
            }
            backStack.add(destination)
        } else {
            backStack.add(destination)
        }
        currentDestination = destination
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

    fun snapshotBackStackRoutes(): List<String> = backStack.map { it.route }

    companion object {
        val Saver = listSaver<WcmpNavigator, String>(
            save = { navigator -> navigator.snapshotBackStackRoutes() },
            restore = { routes ->
                val restoredStack = routes.mapNotNull { WcmpDestination.fromRoute(it) }
                    .ifEmpty { listOf(WcmpDestination.Home) }
                WcmpNavigator(
                    initialDestination = restoredStack.last(),
                    initialBackStack = restoredStack
                )
            }
        )
    }
}

@Composable
fun rememberWcmpNavigator(
    initialDestination: WcmpDestination = WcmpDestination.Home
): WcmpNavigator {
    return rememberSaveable(saver = WcmpNavigator.Saver) {
        WcmpNavigator(initialDestination)
    }
}
