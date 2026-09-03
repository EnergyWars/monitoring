package com.wafflehq.monitoring.ui.pageform

import androidx.lifecycle.SavedStateHandle
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import com.wafflehq.monitoring.data.monitoring.CheckStatus
import com.wafflehq.monitoring.data.monitoring.FakeMonitoredPagesRepository
import com.wafflehq.monitoring.data.monitoring.FakeMonitorHttpClient
import com.wafflehq.monitoring.data.monitoring.PageType
import com.wafflehq.monitoring.ui.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class PageFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock: Clock = Clock.fixed(Instant.parse("2026-09-03T10:00:00Z"), ZoneOffset.UTC)

    @Test
    fun `save with blank fields sets validation errors and does not persist`() = runTest {
        val repository = FakeMonitoredPagesRepository()
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to -1L)), repository, FakeMonitorHttpClient(), clock)

        viewModel.save()

        assertTrue(viewModel.state.value.nameError)
        assertTrue(viewModel.state.value.urlError)
        assertTrue(viewModel.state.value.pathError)
        assertFalse(viewModel.state.value.saved)
    }

    @Test
    fun `save with valid fields inserts a new page`() = runTest {
        val repository = FakeMonitoredPagesRepository()
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to -1L)), repository, FakeMonitorHttpClient(), clock)

        viewModel.onNameChange("Doctolib")
        viewModel.onUrlChange("https://www.doctolib.de/availabilities.json")
        viewModel.onJsonPathChange("availabilities[*].slots[*]")
        viewModel.save()

        assertTrue(viewModel.state.value.saved)
        val inserted = repository.getEnabled().single()
        assertEquals("Doctolib", inserted.name)
        assertEquals(PageType.JSON_API, inserted.type)
    }

    @Test
    fun `url without scheme is rejected`() = runTest {
        val repository = FakeMonitoredPagesRepository()
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to -1L)), repository, FakeMonitorHttpClient(), clock)

        viewModel.onNameChange("Doctolib")
        viewModel.onUrlChange("www.doctolib.de")
        viewModel.onJsonPathChange("a[*]")
        viewModel.save()

        assertTrue(viewModel.state.value.urlError)
        assertFalse(viewModel.state.value.saved)
    }

    @Test
    fun `existing page is loaded for editing`() = runTest {
        val existing = MonitoredPageEntity(
            id = 5,
            name = "Existing",
            type = PageType.JSON_API,
            url = "https://example.test",
            jsonPath = "a[*]",
            enabled = false,
            createdAt = 0L,
            lastCheckedAt = null,
            lastCheckStatus = CheckStatus.NONE,
            lastErrorMessage = null,
        )
        val repository = FakeMonitoredPagesRepository(listOf(existing))
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to 5L)), repository, FakeMonitorHttpClient(), clock)

        assertEquals("Existing", viewModel.state.value.name)
        assertTrue(viewModel.state.value.isEditing)
    }

    @Test
    fun `delete removes the page`() = runTest {
        val existing = MonitoredPageEntity(
            id = 5,
            name = "Existing",
            type = PageType.JSON_API,
            url = "https://example.test",
            jsonPath = "a[*]",
            enabled = true,
            createdAt = 0L,
            lastCheckedAt = null,
            lastCheckStatus = CheckStatus.NONE,
            lastErrorMessage = null,
        )
        val repository = FakeMonitoredPagesRepository(listOf(existing))
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to 5L)), repository, FakeMonitorHttpClient(), clock)

        viewModel.delete()

        assertTrue(viewModel.state.value.deleted)
        assertNull(repository.getById(5))
    }

    @Test
    fun `test now reports match count`() = runTest {
        val repository = FakeMonitoredPagesRepository()
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test", """{"items":[1,2]}""")
        }
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to -1L)), repository, http, clock)
        viewModel.onNameChange("Test")
        viewModel.onUrlChange("https://example.test")
        viewModel.onJsonPathChange("items[*]")

        viewModel.testNow()

        assertEquals(TestResult.Match(2, """{"items":[1,2]}"""), viewModel.state.value.testResult)
    }

    @Test
    fun `test now with no match still exposes the raw response`() = runTest {
        val repository = FakeMonitoredPagesRepository()
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test", """{"items":[]}""")
        }
        val viewModel = PageFormViewModel(SavedStateHandle(mapOf("pageId" to -1L)), repository, http, clock)
        viewModel.onNameChange("Test")
        viewModel.onUrlChange("https://example.test")
        viewModel.onJsonPathChange("items[*]")

        viewModel.testNow()

        assertEquals(TestResult.NoMatch("""{"items":[]}"""), viewModel.state.value.testResult)
    }
}
