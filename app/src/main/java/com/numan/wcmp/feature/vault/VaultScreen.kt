package com.numan.wcmp.feature.vault
import com.numan.wcmp.ui.adaptive.rememberWindowSizeClass
import com.numan.wcmp.ui.adaptive.WindowSizeClass
import com.numan.wcmp.ui.animation.SecureTransition
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.numan.wcmp.domain.evidence.health.EvidenceHealth
import com.numan.wcmp.core.ui.SecureScreenEffect
import com.numan.wcmp.feature.vault.model.VaultCategory
import com.numan.wcmp.feature.vault.model.VaultEvidenceType
import com.numan.wcmp.feature.vault.viewmodel.VaultEvidenceUiModel
import com.numan.wcmp.feature.vault.viewmodel.VaultViewModel
import com.numan.wcmp.feature.vault.viewmodel.VaultViewModelFactory
import com.numan.wcmp.ui.components.PrimaryWcmpButton
import com.numan.wcmp.ui.components.WcmpCard
import com.numan.wcmp.ui.components.WcmpTopBar
import com.numan.wcmp.ui.icons.*
import com.numan.wcmp.core.evidence.SampleEvidenceSeeder
import com.numan.wcmp.data.WcmpRepositoryProvider
import com.numan.wcmp.domain.evidence.EvidenceCaptureRequest
import com.numan.wcmp.domain.evidence.EvidenceTrigger
import com.numan.wcmp.domain.evidence.EvidenceType
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import com.numan.wcmp.feature.vault.components.VaultEvidenceDetails

@Composable
fun VaultScreen(
    modifier: Modifier = Modifier,
    onMenuClick: () -> Unit = {},
    viewModel: VaultViewModel = viewModel(
        factory = VaultViewModelFactory(LocalContext.current)
    )
) {
    SecureScreenEffect()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val coordinator = remember(context) { WcmpRepositoryProvider.getEvidenceCaptureCoordinator(context) }
    
    val windowSizeClass = rememberWindowSizeClass()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VaultCategory.ALL) }
    var selectedEvidence by remember { mutableStateOf<VaultEvidenceUiModel?>(null) }

    val realVaultData by viewModel.evidenceList.collectAsState()
    
    val filteredEvidence = realVaultData.filter { evidenceUiModel ->
        val matchesSearch = evidenceUiModel.evidence.title.contains(searchQuery, ignoreCase = true) ||
                evidenceUiModel.evidence.relatedEvent.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == VaultCategory.ALL || evidenceUiModel.evidence.category == selectedCategory
        matchesSearch && matchesCategory
    }

    val overallHealth = if (realVaultData.isEmpty()) {
        "READY"
    } else if (realVaultData.any { it.health == EvidenceHealth.INTEGRITY_ERROR || it.health == EvidenceHealth.KEY_UNAVAILABLE }) {
        "LIMITED"
    } else {
        "ENCRYPTION ACTIVE"
    }

    val listContent = @Composable {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
        ) {
            item {
                VaultSecurityHero(
                    overallHealth = overallHealth,
                    evidenceCount = realVaultData.size,
                    photosCount = realVaultData.count { it.evidence.type == VaultEvidenceType.PHOTO },
                    audioCount = realVaultData.count { it.evidence.type == VaultEvidenceType.AUDIO },
                    locationCount = realVaultData.count { it.evidence.type == VaultEvidenceType.LOCATION }
                )
            }
            item {
                VaultQuickCaptureBar(
                    onCapturePhoto = {
                        coroutineScope.launch {
                            coordinator.requestCapture(
                                EvidenceCaptureRequest(
                                    trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                    preferredType = EvidenceType.FRONT_CAMERA_PHOTO
                                )
                            )
                        }
                    },
                    onCaptureAudio = {
                        coroutineScope.launch {
                            coordinator.requestCapture(
                                EvidenceCaptureRequest(
                                    trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                    preferredType = EvidenceType.AUDIO_CLIP
                                )
                            )
                        }
                    },
                    onCaptureLocation = {
                        coroutineScope.launch {
                            coordinator.requestCapture(
                                EvidenceCaptureRequest(
                                    trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                    preferredType = EvidenceType.LOCATION_STAMP
                                )
                            )
                        }
                    },
                    onSeedSample = {
                        coroutineScope.launch {
                            val count = SampleEvidenceSeeder.seed(context)
                            android.widget.Toast.makeText(context, "Added $count verified evidence records", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
            item {
                VaultSearchBox(query = searchQuery, onQueryChanged = { searchQuery = it })
            }
            item {
                VaultCategoryFilters(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            if (filteredEvidence.isEmpty()) {
                item { 
                    VaultEmptyState(
                        isSearch = searchQuery.isNotEmpty(),
                        onCapturePhoto = {
                            coroutineScope.launch {
                                coordinator.requestCapture(
                                    EvidenceCaptureRequest(
                                        trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                        preferredType = EvidenceType.FRONT_CAMERA_PHOTO
                                    )
                                )
                            }
                        },
                        onCaptureAudio = {
                            coroutineScope.launch {
                                coordinator.requestCapture(
                                    EvidenceCaptureRequest(
                                        trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                        preferredType = EvidenceType.AUDIO_CLIP
                                    )
                                )
                            }
                        },
                        onCaptureLocation = {
                            coroutineScope.launch {
                                coordinator.requestCapture(
                                    EvidenceCaptureRequest(
                                        trigger = EvidenceTrigger.MANUAL_CAPTURE,
                                        preferredType = EvidenceType.LOCATION_STAMP
                                    )
                                )
                            }
                        },
                        onSeedSample = {
                            coroutineScope.launch {
                                val count = SampleEvidenceSeeder.seed(context)
                                android.widget.Toast.makeText(context, "Added $count verified evidence records", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) 
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Evidence",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Newest First",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                items(filteredEvidence, key = { it.evidence.id }) { evidence ->
                    VaultEvidenceCardV2(
                        evidenceUiModel = evidence,
                        onClick = { selectedEvidence = evidence }
                    )
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            WcmpTopBar(
                title = "EVIDENCE VAULT",
                subtitle = "Protected security evidence",
                onMenuClick = onMenuClick
            )

            if (windowSizeClass == WindowSizeClass.EXPANDED) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        listContent()
                    }
                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant))
                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        if (selectedEvidence != null) {
                            VaultEvidenceDetails(
                                evidenceUiModel = selectedEvidence!!,
                                onBack = { selectedEvidence = null },
                                onDelete = { 
                                    selectedEvidence?.let { ev: VaultEvidenceUiModel -> 
                                        coroutineScope.launch {
                                            val result = viewModel.deleteEvidence(ev.evidence)
                                            if (result !is com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.Success) {
                                                android.widget.Toast.makeText(context, "Deletion failed", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                    selectedEvidence = null 
                                }
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Select an evidence item to view details",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            } else {
                listContent()
            }
        }

        if (windowSizeClass != WindowSizeClass.EXPANDED) {
            AnimatedVisibility(
                visible = selectedEvidence != null,
                enter = SecureTransition.enterFadeScale,
                exit = SecureTransition.exitFadeScale,
                modifier = Modifier.fillMaxSize()
            ) {
                selectedEvidence?.let { evidence ->
                    VaultEvidenceDetails(
                        evidenceUiModel = evidence,
                        onBack = { selectedEvidence = null },
                        onDelete = { 
                            selectedEvidence?.let { ev: VaultEvidenceUiModel -> 
                                coroutineScope.launch {
                                    val result = viewModel.deleteEvidence(ev.evidence)
                                    if (result !is com.numan.wcmp.feature.vault.model.EvidenceDeletionResult.Success) {
                                        android.widget.Toast.makeText(context, "Deletion failed", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            selectedEvidence = null 
                        }
                    )
                }
            }
        }
    }
} 

@Composable
private fun VaultSecurityHero(
    overallHealth: String,
    evidenceCount: Int,
    photosCount: Int,
    audioCount: Int,
    locationCount: Int
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentPadding = 20.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ring with Shield
                val ringColor = if (overallHealth == "ENCRYPTION ACTIVE" || overallHealth == "READY") {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ringColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ringColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = WcmpIcons.ShieldCheck,
                            contentDescription = null,
                            tint = ringColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = overallHealth,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = ringColor
                    )
                    if (overallHealth == "ENCRYPTION ACTIVE" || overallHealth == "READY") {
                        Text(
                            text = "AES-GCM â€¢ Android Keystore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(ringColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (overallHealth == "LIMITED") "LIMITED" else "SECURE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ringColor
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            
            // Right Side: Summary
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "TOTAL EVIDENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = evidenceCount.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    SummaryIcon(WcmpIcons.Eye, photosCount.toString(), MaterialTheme.colorScheme.onSurfaceVariant)
                    SummaryIcon(WcmpIcons.Mic, audioCount.toString(), MaterialTheme.colorScheme.primary)
                    SummaryIcon(WcmpIcons.LocationOn, locationCount.toString(), MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
private fun SummaryIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VaultSearchBox(query: String, onQueryChanged: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = WcmpIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search evidence...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
            if (query.isNotEmpty()) {
                Icon(
                    imageVector = WcmpIcons.Close,
                    contentDescription = "Clear search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onQueryChanged("") }
                )
            }
        }
    }
}

@Composable
private fun VaultCategoryFilters(
    selectedCategory: VaultCategory,
    onCategorySelected: (VaultCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val categories = VaultCategory.values()
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surface
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun VaultEvidenceCardV2(
    evidenceUiModel: VaultEvidenceUiModel,
    onClick: () -> Unit
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        contentPadding = 16.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val iconBg = MaterialTheme.colorScheme.surfaceVariant
            val iconTint = MaterialTheme.colorScheme.primary
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (evidenceUiModel.evidence.type) {
                        VaultEvidenceType.PHOTO -> WcmpIcons.Eye
                        VaultEvidenceType.AUDIO -> WcmpIcons.Mic
                        VaultEvidenceType.LOCATION -> WcmpIcons.LocationOn
                        else -> WcmpIcons.ShieldCheck
                    },
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Middle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evidenceUiModel.evidence.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = evidenceUiModel.evidence.relatedEvent,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = evidenceUiModel.evidence.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Badge
            val health = evidenceUiModel.health
            val badgeColor = when (health) {
                EvidenceHealth.HEALTHY -> MaterialTheme.colorScheme.tertiary
                EvidenceHealth.LEGACY -> MaterialTheme.colorScheme.secondary // Amber replacement? Use secondary or hardcoded warning
                else -> MaterialTheme.colorScheme.error
            }
            val badgeText = when (health) {
                EvidenceHealth.HEALTHY -> "ENCRYPTED"
                EvidenceHealth.LEGACY -> "LEGACY"
                EvidenceHealth.INTEGRITY_ERROR -> "INTEGRITY ERROR"
                EvidenceHealth.MISSING_FILE -> "FILE MISSING"
                EvidenceHealth.KEY_UNAVAILABLE -> "KEY LOST"
                else -> "UNKNOWN"
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = if (health == EvidenceHealth.HEALTHY) WcmpIcons.ShieldCheck else WcmpIcons.ShieldAlert,
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = badgeColor
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = WcmpIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun VaultEmptyState(
    isSearch: Boolean,
    onCapturePhoto: () -> Unit = {},
    onCaptureAudio: () -> Unit = {},
    onCaptureLocation: () -> Unit = {},
    onSeedSample: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp, bottom = 40.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 24.dp)) {
            Icon(
                imageVector = WcmpIcons.ShieldCheck,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearch) "NO EVIDENCE FOUND" else "EVIDENCE VAULT READY",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSearch) "Try adjusting your search or filters." else "No security evidence recorded yet.\nCapture real evidence using the buttons below, or generate a sample set to test the vault:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (!isSearch) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onCapturePhoto,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = WcmpIcons.Camera, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo", style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = onCaptureAudio,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = WcmpIcons.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Audio", style = MaterialTheme.typography.labelSmall)
                    }
                    FilledTonalButton(
                        onClick = onCaptureLocation,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 10.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = WcmpIcons.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("GPS", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSeedSample,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Text(
                        text = "+ Generate Sample Evidence Set",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultQuickCaptureBar(
    onCapturePhoto: () -> Unit,
    onCaptureAudio: () -> Unit,
    onCaptureLocation: () -> Unit,
    onSeedSample: () -> Unit
) {
    WcmpCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        contentPadding = 14.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUICK CAPTURE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onSeedSample,
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "+ Sample Set",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onCapturePhoto,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = WcmpIcons.Camera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Photo", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = onCaptureAudio,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = WcmpIcons.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Audio", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                    onClick = onCaptureLocation,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = WcmpIcons.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GPS", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}



