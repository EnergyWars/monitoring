package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import kotlinx.coroutines.flow.Flow

interface MonitoredPagesRepository {
    fun observeAll(): Flow<List<MonitoredPageEntity>>
    fun observeById(id: Long): Flow<MonitoredPageEntity?>
    suspend fun getById(id: Long): MonitoredPageEntity?
    suspend fun getEnabled(): List<MonitoredPageEntity>
    suspend fun insert(page: MonitoredPageEntity): Long
    suspend fun update(page: MonitoredPageEntity)
    suspend fun delete(page: MonitoredPageEntity)
    suspend fun updateCheckResult(id: Long, checkedAt: Long, status: CheckStatus, errorMessage: String?)
}
