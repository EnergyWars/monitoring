package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMonitoredPagesRepository(initial: List<MonitoredPageEntity> = emptyList()) : MonitoredPagesRepository {

    private val pages = MutableStateFlow(initial.associateBy { it.id })
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeAll(): Flow<List<MonitoredPageEntity>> = pages.map { it.values.toList() }

    override fun observeById(id: Long): Flow<MonitoredPageEntity?> = pages.map { it[id] }

    override suspend fun getById(id: Long): MonitoredPageEntity? = pages.value[id]

    override suspend fun getEnabled(): List<MonitoredPageEntity> = pages.value.values.filter { it.enabled }

    override suspend fun insert(page: MonitoredPageEntity): Long {
        val id = nextId++
        pages.value = pages.value + (id to page.copy(id = id))
        return id
    }

    override suspend fun update(page: MonitoredPageEntity) {
        pages.value = pages.value + (page.id to page)
    }

    override suspend fun delete(page: MonitoredPageEntity) {
        pages.value = pages.value - page.id
    }

    override suspend fun updateCheckResult(id: Long, checkedAt: Long, status: CheckStatus, errorMessage: String?) {
        val page = pages.value[id] ?: return
        pages.value = pages.value + (id to page.copy(
            lastCheckedAt = checkedAt,
            lastCheckStatus = status,
            lastErrorMessage = errorMessage,
        ))
    }
}
