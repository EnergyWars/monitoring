package com.wafflehq.monitoring.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflehq.monitoring.background.ReliabilityChecker
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import com.wafflehq.monitoring.data.monitoring.MonitoredPagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val pagesRepository: MonitoredPagesRepository,
    private val reliabilityChecker: ReliabilityChecker,
) : ViewModel() {

    val pages: StateFlow<List<MonitoredPageEntity>> = pagesRepository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val _reliabilityConfigured = MutableStateFlow(true)
    val reliabilityConfigured: StateFlow<Boolean> = _reliabilityConfigured.asStateFlow()

    fun refreshReliabilityStatus() {
        _reliabilityConfigured.value = reliabilityChecker.status().isFullyConfigured
    }

    fun setEnabled(page: MonitoredPageEntity, enabled: Boolean) {
        viewModelScope.launch {
            pagesRepository.update(page.copy(enabled = enabled))
        }
    }
}
