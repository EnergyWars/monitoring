package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.data.db.CheckResultDao
import com.wafflehq.monitoring.data.db.CheckResultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomCheckResultsRepository @Inject constructor(
    private val dao: CheckResultDao,
) : CheckResultsRepository {

    override fun observeForPage(pageId: Long): Flow<List<CheckResultEntity>> = dao.observeForPage(pageId)

    override fun observeOpenCountForPage(pageId: Long): Flow<Int> = dao.observeOpenCountForPage(pageId)

    override suspend fun getAllUnacknowledged(): List<CheckResultEntity> = dao.getAllUnacknowledged()

    override suspend fun insert(result: CheckResultEntity): Long = dao.insert(result)

    override suspend fun acknowledgeAllForPage(pageId: Long) = dao.acknowledgeAllForPage(pageId)

    override suspend fun updateLastNotifiedAt(ids: List<Long>, now: Long) = dao.updateLastNotifiedAt(ids, now)
}
