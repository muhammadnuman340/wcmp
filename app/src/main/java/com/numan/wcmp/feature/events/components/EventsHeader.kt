package com.numan.wcmp.feature.events.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.numan.wcmp.ui.components.WcmpTopBar

@Composable
fun EventsHeader(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {}
) {
    WcmpTopBar(
        modifier = modifier,
        title = "Events",
        subtitle = "Security activity & detected events",
        onMenuClick = onMenuClick
    )
}
