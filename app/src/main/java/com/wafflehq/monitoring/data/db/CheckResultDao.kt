package com.wafflehq.monitoring.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckResultDao {

    @Query("SELECT * FROM check_results WHERE pageId = :pageId ORDER BY triggeredAt DESC")
    fun observeForPage(pageId: Long): Flow<List<CheckResultEntity>>

    @Query("SELECT * FROM check_results WHERE acknowledged = 0")
    suspend fun getAllUnacknowledged(): List<CheckResultEntity>

    @Insert
    suspend fun insert(result: CheckResultEntity): Long

    @Query("UPDATE check_results SET acknowledged = 1 WHERE pageId = :pageId AND acknowledged = 0")
    suspend fun acknowledgeAllForPage(pageId: Long)

    @Query("UPDATE check_results SET lastNotifiedAt = :now WHERE id IN (:ids)")
    suspend fun updateLastNotifiedAt(ids: List<Long>, now: Long)

    @Query("SELECT COUNT(*) FROM check_results WHERE pageId = :pageId AND acknowledged = 0")
    fun observeOpenCountForPage(pageId: Long): Flow<Int>
}
