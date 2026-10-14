package com.numan.wcmp.feature.events
import com.numan.wcmp.ui.adaptive.rememberWindowSizeClass
import com.numan.wcmp.ui.adaptive.WindowSizeClass
import com.numan.wcmp.ui.animation.SecureTransition
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.numan.wcmp.feature.events.components.EventCard
import com.numan.wcmp.feature.events.components.EventDetailsSheet
import com.numan.wcmp.feature.events.components.EventEmptyState
import com.numan.wcmp.feature.events.components.EventFilterBar
import com.numan.wcmp.feature.events.components.DateFilterBar
import com.numan.wcmp.feature.events.components.EventSummarySection
import com.numan.wcmp.feature.events.components.EventsHeader
import com.numan.wcmp.feature.events.model.EventFilter
import com.numan.wcmp.feature.events.model.EventSeverity
import com.numan.wcmp.feature.events.model.SecurityEvent
import com.numan.wcmp.ui.icons.WcmpIcons

@Composable
fun EventsScreen(
    modifier: Modifier = Modifier,
    viewModel: EventsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = EventsViewModel.Factory(com.numan.wcmp.data.WcmpRepositoryProvider.getSecurityRepository(androidx.compose.ui.platform.LocalContext.current))
    )
) {
    // UI State
    var selectedFilter by remember { mutableStateOf(EventFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEvent by remember { mutableStateOf<SecurityEvent?>(null) }
    
    val allEvents by viewModel.events.collectAsState()
    val windowSizeClass = rememberWindowSizeClass()

    // Simple UI Filtering
    val filteredEvents = allEvents.filter { event ->
        val matchesSearch = event.title.contains(searchQuery, ignoreCase = true) ||
                event.triggerSource.contains(searchQuery, ignoreCase = true)
                
        val matchesFilter = when (selectedFilter) {
            EventFilter.ALL -> true
            EventFilter.INTRUDER -> event.severity == EventSeverity.HIGH || event.severity == EventSeverity.CRITICAL
            EventFilter.APP_ACCESS -> event.icon == WcmpIcons.Apps
            EventFilter.DEVICE -> event.icon == WcmpIcons.Radar
            EventFilter.WCMP -> event.icon == WcmpIcons.ShieldCheck
        }
        
        matchesSearch && matchesFilter
    }

    // Derive summary metrics from real data
    val todayCount = allEvents.count { event ->
        val ts = event.timestamp.lowercase()
        ts.contains("now") || ts.contains("min") || ts.contains("hour") || ts.contains("today")
    }

    val listContent = @Composable {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                bottom = 32.dp
            )
        ) {
            item {
                EventSummarySection(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    totalEvents = allEvents.size,
                    evidenceCount = allEvents.sumOf { it.evidenceCount },
                    todayCount = todayCount,
                    activeCount = 0
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                // Search Control
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search events, triggers, IDs...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                DateFilterBar()
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                EventFilterBar(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (filteredEvents.isEmpty()) {
                item {
                    EventEmptyState(
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            } else {
                items(filteredEvents) { event ->
                    EventCard(
                        event = event,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        onClick = { selectedEvent = event }
                    )
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EventsHeader(
            onMenuClick = { /* Handle Menu Click */ }
        )

        if (windowSizeClass == WindowSizeClass.EXPANDED) {
            // Adaptive dual-pane layout for tablets/foldables
            Row(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    listContent()
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (selectedEvent != null) {
                        EventDetailsSheet(
                            event = selectedEvent!!,
                            onDismiss = { selectedEvent = null }
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select an event to view details",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        } else {
            listContent()
        }
    }

    // Detail Sheet (compact mode only)
    if (windowSizeClass != WindowSizeClass.EXPANDED) {
        AnimatedVisibility(
            visible = selectedEvent != null,
            enter = SecureTransition.enterFadeScale,
            exit = SecureTransition.exitFadeScale
        ) {
            selectedEvent?.let { event ->
                EventDetailsSheet(
                    event = event,
                    onDismiss = { selectedEvent = null }
                )
            }
        }
    }
}
