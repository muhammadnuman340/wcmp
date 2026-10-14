package com.numan.wcmp.feature.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.numan.wcmp.data.local.database.entity.SecurityEventEntity
import com.numan.wcmp.data.repository.SecurityRepository
import com.numan.wcmp.feature.events.model.EventSeverity
import com.numan.wcmp.feature.events.model.SecurityEvent
import com.numan.wcmp.ui.icons.WcmpIcons
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventsViewModel(private val repository: SecurityRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    val events: StateFlow<List<SecurityEvent>> = repository.observeSecurityEvents()
        .map { entities -> entities.map { it.toUiModel() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun SecurityEventEntity.toUiModel(): SecurityEvent {
        return SecurityEvent(
            id = this.id,
            sessionId = this.sessionId,
            title = this.title,
            description = this.description,
            triggerSource = this.trigger,
            packageName = this.packageName,
            applicationLabel = this.applicationLabel,
            evidenceCount = this.evidenceCount,
            duration = if (this.duration > 0) "${this.duration / 1000} sec" else "",
            timestamp = dateFormat.format(Date(this.timestamp)),
            type = this.type,
            status = this.status,
            metadata = this.metadata,
            severity = when (this.severity) {
                "HIGH" -> EventSeverity.HIGH
                "MEDIUM" -> EventSeverity.MEDIUM
                "LOW" -> EventSeverity.LOW
                "INFO" -> EventSeverity.INFO
                "CRITICAL" -> EventSeverity.CRITICAL
                else -> EventSeverity.INFO
            },
            icon = when (this.type) {
                "PROTECTED_APP_ACCESS" -> WcmpIcons.Apps
                "DEVICE_MOVEMENT" -> WcmpIcons.Radar
                "MANUAL_TEST" -> WcmpIcons.Settings
                else -> WcmpIcons.ShieldCheck
            }
        )
    }

    class Factory(private val repository: SecurityRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EventsViewModel(repository) as T
        }
    }
}
