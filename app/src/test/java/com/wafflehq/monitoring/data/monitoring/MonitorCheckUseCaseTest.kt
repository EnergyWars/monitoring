package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.background.FakeNotifier
import com.wafflehq.monitoring.background.RENOTIFY_INTERVAL_MILLIS
import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class MonitorCheckUseCaseTest {

    private val fixedNow = Instant.parse("2026-09-03T10:00:00Z")
    private val clock = Clock.fixed(fixedNow, ZoneOffset.UTC)

    private fun page(id: Long = 1, url: String = "https://example.test/api", jsonPath: String = "items[*]") =
        MonitoredPageEntity(
            id = id,
            name = "Test page",
            type = PageType.JSON_API,
            url = url,
            jsonPath = jsonPath,
            enabled = true,
            createdAt = fixedNow.toEpochMilli(),
            lastCheckedAt = null,
            lastCheckStatus = CheckStatus.NONE,
            lastErrorMessage = null,
        )

    @Test
    fun `match found inserts result and notifies once`() = runTest {
        val pages = FakeMonitoredPagesRepository(listOf(page()))
        val results = FakeCheckResultsRepository()
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test/api", """{"items":[1,2,3]}""")
        }
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertEquals(1, results.all().size)
        assertEquals(3, results.all().single().matchCount)
        assertEquals(1, notifier.notifications.size)
        assertEquals(CheckStatus.OK_MATCH, pages.getById(1)?.lastCheckStatus)
    }

    @Test
    fun `no match updates status without notifying`() = runTest {
        val pages = FakeMonitoredPagesRepository(listOf(page()))
        val results = FakeCheckResultsRepository()
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test/api", """{"items":[]}""")
        }
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertTrue(results.all().isEmpty())
        assertTrue(notifier.notifications.isEmpty())
        assertEquals(CheckStatus.OK_NO_MATCH, pages.getById(1)?.lastCheckStatus)
    }

    @Test
    fun `http error sets error status without notifying`() = runTest {
        val pages = FakeMonitoredPagesRepository(listOf(page()))
        val results = FakeCheckResultsRepository()
        val http = FakeMonitorHttpClient().apply {
            failWith("https://example.test/api", "timeout")
        }
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertTrue(results.all().isEmpty())
        assertTrue(notifier.notifications.isEmpty())
        val updated = pages.getById(1)
        assertEquals(CheckStatus.ERROR, updated?.lastCheckStatus)
        assertEquals("timeout", updated?.lastErrorMessage)
    }

    @Test
    fun `malformed json sets error status`() = runTest {
        val pages = FakeMonitoredPagesRepository(listOf(page()))
        val results = FakeCheckResultsRepository()
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test/api", "not json")
        }
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertEquals(CheckStatus.ERROR, pages.getById(1)?.lastCheckStatus)
        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `disabled page is not checked`() = runTest {
        val pages = FakeMonitoredPagesRepository(listOf(page().copy(enabled = false)))
        val results = FakeCheckResultsRepository()
        val http = FakeMonitorHttpClient()
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertNull(pages.getById(1)?.lastCheckedAt)
        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `unacknowledged result younger than an hour is not renotified`() = runTest {
        val testPage = page(id = 1)
        val pages = FakeMonitoredPagesRepository(listOf(testPage))
        val results = FakeCheckResultsRepository()
        results.insert(
            CheckResultEntity(
                pageId = 1,
                triggeredAt = fixedNow.toEpochMilli() - 1_000,
                matchedSummary = "[1]",
                matchCount = 1,
                acknowledged = false,
                lastNotifiedAt = fixedNow.toEpochMilli() - (RENOTIFY_INTERVAL_MILLIS / 2),
            ),
        )
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test/api", """{"items":[]}""")
        }
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertTrue(notifier.notifications.isEmpty())
    }

    @Test
    fun `unacknowledged result at least an hour old is renotified`() = runTest {
        val testPage = page(id = 1)
        val pages = FakeMonitoredPagesRepository(listOf(testPage))
        val results = FakeCheckResultsRepository()
        val staleNotifiedAt = fixedNow.toEpochMilli() - RENOTIFY_INTERVAL_MILLIS
        results.insert(
            CheckResultEntity(
                pageId = 1,
                triggeredAt = staleNotifiedAt,
                matchedSummary = "[1]",
                matchCount = 1,
                acknowledged = false,
                lastNotifiedAt = staleNotifiedAt,
            ),
        )
        val http = FakeMonitorHttpClient().apply {
            respondWith("https://example.test/api", """{"items":[]}""")
        }
        val notifier = FakeNotifier()
        val useCase = MonitorCheckUseCase(pages, results, http, notifier, clock)

        useCase.runChecks()

        assertEquals(1, notifier.notifications.size)
        assertEquals(fixedNow.toEpochMilli(), results.all().single().lastNotifiedAt)
        assertFalse(results.all().single().acknowledged)
    }
}
