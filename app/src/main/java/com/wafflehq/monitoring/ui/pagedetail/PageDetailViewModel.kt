package com.wafflehq.monitoring.ui.pagedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import com.wafflehq.monitoring.data.monitoring.CheckResultsRepository
import com.wafflehq.monitoring.data.monitoring.MonitoredPagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PageDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pagesRepository: MonitoredPagesRepository,
    private val resultsRepository: CheckResultsRepository,
) : ViewModel() {

    private val pageId: Long = checkNotNull(savedStateHandle["pageId"])

    val page: StateFlow<MonitoredPageEntity?> = pagesRepository.observeById(pageId)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = null)

    val results: StateFlow<List<CheckResultEntity>> = resultsRepository.observeForPage(pageId)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = emptyList())

    fun acknowledgeAll() {
        viewModelScope.launch {
            resultsRepository.acknowledgeAllForPage(pageId)
        }
    }
}
