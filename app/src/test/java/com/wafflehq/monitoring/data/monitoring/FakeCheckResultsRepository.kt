package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.data.db.CheckResultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCheckResultsRepository : CheckResultsRepository {

    private val results = MutableStateFlow(emptyMap<Long, CheckResultEntity>())
    private var nextId = 1L

    override fun observeForPage(pageId: Long): Flow<List<CheckResultEntity>> =
        results.map { map -> map.values.filter { it.pageId == pageId }.sortedByDescending { it.triggeredAt } }

    override fun observeOpenCountForPage(pageId: Long): Flow<Int> =
        results.map { map -> map.values.count { it.pageId == pageId && !it.acknowledged } }

    override suspend fun getAllUnacknowledged(): List<CheckResultEntity> =
        results.value.values.filter { !it.acknowledged }

    override suspend fun insert(result: CheckResultEntity): Long {
        val id = nextId++
        results.value = results.value + (id to result.copy(id = id))
        return id
    }

    override suspend fun acknowledgeAllForPage(pageId: Long) {
        results.value = results.value.mapValues { (_, result) ->
            if (result.pageId == pageId) result.copy(acknowledged = true) else result
        }
    }

    override suspend fun updateLastNotifiedAt(ids: List<Long>, now: Long) {
        results.value = results.value.mapValues { (id, result) ->
            if (id in ids) result.copy(lastNotifiedAt = now) else result
        }
    }

    fun all(): List<CheckResultEntity> = results.value.values.toList()
}
