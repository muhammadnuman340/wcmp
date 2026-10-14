package com.numan.wcmp.feature.home
import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.numan.wcmp.ui.components.IconWcmpButton
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.icons.WcmpIcons
import com.numan.wcmp.ui.theme.spacing

@Composable
fun QuickActionsSection(
    modifier: Modifier = Modifier,
    onNavigateToEvents: () -> Unit,
    onNavigateToProtection: () -> Unit,
    onNavigateToVault: () -> Unit = {},
    onNavigateToProtectedApps: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "QUICK ACTIONS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ActionCard(
                title = "Events",
                icon = WcmpIcons.History,
                onClick = onNavigateToEvents,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionCard(
                title = "Evidence",
                icon = WcmpIcons.Lock,
                onClick = onNavigateToVault,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionCard(
                title = "Protection",
                icon = WcmpIcons.Shield,
                onClick = onNavigateToProtection,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            ActionCard(
                title = "App Lock",
                icon = WcmpIcons.Apps,
                onClick = onNavigateToProtectedApps,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    WcmpCard(
        modifier = modifier.aspectRatio(1f),
        contentPadding = 8.dp,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconWcmpButton(
                onClick = onClick,
                icon = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
