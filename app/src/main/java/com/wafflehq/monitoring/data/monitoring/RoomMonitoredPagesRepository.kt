package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.data.db.MonitoredPageDao
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomMonitoredPagesRepository @Inject constructor(
    private val dao: MonitoredPageDao,
) : MonitoredPagesRepository {

    override fun observeAll(): Flow<List<MonitoredPageEntity>> = dao.observeAll()

    override fun observeById(id: Long): Flow<MonitoredPageEntity?> = dao.observeById(id)

    override suspend fun getById(id: Long): MonitoredPageEntity? = dao.getById(id)

    override suspend fun getEnabled(): List<MonitoredPageEntity> = dao.getEnabled()

    override suspend fun insert(page: MonitoredPageEntity): Long = dao.insert(page)

    override suspend fun update(page: MonitoredPageEntity) = dao.update(page)

    override suspend fun delete(page: MonitoredPageEntity) = dao.delete(page)

    override suspend fun updateCheckResult(id: Long, checkedAt: Long, status: CheckStatus, errorMessage: String?) =
        dao.updateCheckResult(id, checkedAt, status, errorMessage)
}
