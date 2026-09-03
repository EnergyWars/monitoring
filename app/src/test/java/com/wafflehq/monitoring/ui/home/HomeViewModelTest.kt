package com.wafflehq.monitoring.ui.home

import com.wafflehq.monitoring.background.ReliabilityChecker
import com.wafflehq.monitoring.background.ReliabilityStatus
import com.wafflehq.monitoring.data.monitoring.CheckStatus
import com.wafflehq.monitoring.data.monitoring.FakeMonitoredPagesRepository
import com.wafflehq.monitoring.data.monitoring.PageType
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import com.wafflehq.monitoring.ui.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun page(id: Long, enabled: Boolean = true) = MonitoredPageEntity(
        id = id,
        name = "Page $id",
        type = PageType.JSON_API,
        url = "https://example.test",
        jsonPath = "items[*]",
        enabled = enabled,
        createdAt = 0L,
        lastCheckedAt = null,
        lastCheckStatus = CheckStatus.NONE,
        lastErrorMessage = null,
    )

    @Test
    fun `setEnabled updates the page in the repository`() = runTest {
        val repository = FakeMonitoredPagesRepository(listOf(page(id = 1, enabled = true)))
        val checker = mockk<ReliabilityChecker>()
        val viewModel = HomeViewModel(repository, checker)

        viewModel.setEnabled(page(id = 1, enabled = true), false)

        assertFalse(repository.getById(1)!!.enabled)
    }

    @Test
    fun `refreshReliabilityStatus reflects checker result`() {
        val repository = FakeMonitoredPagesRepository(emptyList())
        val checker = mockk<ReliabilityChecker>()
        every { checker.status() } returns ReliabilityStatus(
            notificationsGranted = false,
            batteryOptimizationIgnored = true,
            exactAlarmsAllowed = true,
            showAutostart = false,
        )
        val viewModel = HomeViewModel(repository, checker)

        viewModel.refreshReliabilityStatus()

        assertFalse(viewModel.reliabilityConfigured.value)
    }
}
