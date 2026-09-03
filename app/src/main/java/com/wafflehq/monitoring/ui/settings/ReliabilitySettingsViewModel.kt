package com.wafflehq.monitoring.ui.settings

import androidx.lifecycle.ViewModel
import com.wafflehq.monitoring.background.ReliabilityChecker
import com.wafflehq.monitoring.background.ReliabilityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReliabilitySettingsViewModel @Inject constructor(
    private val reliabilityChecker: ReliabilityChecker,
) : ViewModel() {

    private val _status = MutableStateFlow(reliabilityChecker.status())
    val status: StateFlow<ReliabilityStatus> = _status.asStateFlow()

    fun refresh() {
        _status.value = reliabilityChecker.status()
    }
}
