package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.data.db.CheckResultEntity
import kotlinx.coroutines.flow.Flow

interface CheckResultsRepository {
    fun observeForPage(pageId: Long): Flow<List<CheckResultEntity>>
    fun observeOpenCountForPage(pageId: Long): Flow<Int>
    suspend fun getAllUnacknowledged(): List<CheckResultEntity>
    suspend fun insert(result: CheckResultEntity): Long
    suspend fun acknowledgeAllForPage(pageId: Long)
    suspend fun updateLastNotifiedAt(ids: List<Long>, now: Long)
}
