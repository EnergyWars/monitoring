package com.wafflehq.monitoring.ui.pageform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import com.wafflehq.monitoring.data.monitoring.CheckStatus
import com.wafflehq.monitoring.data.monitoring.JsonPathEvaluator
import com.wafflehq.monitoring.data.monitoring.MonitorHttpClient
import com.wafflehq.monitoring.data.monitoring.MonitoredPagesRepository
import com.wafflehq.monitoring.data.monitoring.PageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Inject

sealed interface TestResult {
    data object Idle : TestResult
    data object Running : TestResult
    data class Match(val count: Int, val rawResponse: String) : TestResult
    data class NoMatch(val rawResponse: String) : TestResult
    data class Error(val message: String, val rawResponse: String? = null) : TestResult
}

data class PageFormState(
    val pageId: Long? = null,
    val name: String = "",
    val url: String = "",
    val jsonPath: String = "",
    val enabled: Boolean = true,
    val nameError: Boolean = false,
    val urlError: Boolean = false,
    val pathError: Boolean = false,
    val testResult: TestResult = TestResult.Idle,
    val saved: Boolean = false,
    val deleted: Boolean = false,
) {
    val isEditing: Boolean get() = pageId != null
}

@HiltViewModel
class PageFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pagesRepository: MonitoredPagesRepository,
    private val httpClient: MonitorHttpClient,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(PageFormState())
    val state: StateFlow<PageFormState> = _state.asStateFlow()

    init {
        val pageId: Long? = savedStateHandle.get<Long>("pageId")?.takeIf { it > 0 }
        if (pageId != null) {
            viewModelScope.launch {
                pagesRepository.getById(pageId)?.let { page ->
                    _state.update {
                        it.copy(
                            pageId = page.id,
                            name = page.name,
                            url = page.url,
                            jsonPath = page.jsonPath,
                            enabled = page.enabled,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) = _state.update { it.copy(name = value, nameError = false) }
    fun onUrlChange(value: String) = _state.update { it.copy(url = value, urlError = false) }
    fun onJsonPathChange(value: String) = _state.update { it.copy(jsonPath = value, pathError = false) }
    fun onEnabledChange(value: Boolean) = _state.update { it.copy(enabled = value) }

    fun testNow() {
        val current = _state.value
        if (!validate(current)) return

        _state.update { it.copy(testResult = TestResult.Running) }
        viewModelScope.launch {
            val result = runTest(current.url, current.jsonPath)
            _state.update { it.copy(testResult = result) }
        }
    }

    private suspend fun runTest(url: String, jsonPath: String): TestResult {
        val body = httpClient.get(url).getOrElse { error ->
            return TestResult.Error(error.message ?: error.toString())
        }
        return try {
            val matches = JsonPathEvaluator.evaluate(Json.parseToJsonElement(body), jsonPath)
            if (matches.isEmpty()) TestResult.NoMatch(body) else TestResult.Match(matches.size, body)
        } catch (e: SerializationException) {
            TestResult.Error(e.message ?: e.toString(), rawResponse = body)
        }
    }

    fun save() {
        val current = _state.value
        if (!validate(current)) return

        viewModelScope.launch {
            val now = clock.millis()
            if (current.pageId == null) {
                pagesRepository.insert(
                    MonitoredPageEntity(
                        name = current.name.trim(),
                        type = PageType.JSON_API,
                        url = current.url.trim(),
                        jsonPath = current.jsonPath.trim(),
                        enabled = current.enabled,
                        createdAt = now,
                        lastCheckedAt = null,
                        lastCheckStatus = CheckStatus.NONE,
                        lastErrorMessage = null,
                    ),
                )
            } else {
                val existing = pagesRepository.getById(current.pageId) ?: return@launch
                pagesRepository.update(
                    existing.copy(
                        name = current.name.trim(),
                        url = current.url.trim(),
                        jsonPath = current.jsonPath.trim(),
                        enabled = current.enabled,
                    ),
                )
            }
            _state.update { it.copy(saved = true) }
        }
    }

    fun delete() {
        val pageId = _state.value.pageId ?: return
        viewModelScope.launch {
            pagesRepository.getById(pageId)?.let { pagesRepository.delete(it) }
            _state.update { it.copy(deleted = true) }
        }
    }

    private fun validate(state: PageFormState): Boolean {
        val nameError = state.name.isBlank()
        val urlError = state.url.isBlank() || !isValidUrl(state.url)
        val pathError = state.jsonPath.isBlank()
        _state.update { it.copy(nameError = nameError, urlError = urlError, pathError = pathError) }
        return !nameError && !urlError && !pathError
    }

    private fun isValidUrl(url: String): Boolean =
        url.startsWith("http://") || url.startsWith("https://")
}
