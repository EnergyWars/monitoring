package com.wafflehq.monitoring.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wafflehq.monitoring.data.monitoring.CheckStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredPageDao {

    @Query("SELECT * FROM monitored_pages ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<MonitoredPageEntity>>

    @Query("SELECT * FROM monitored_pages WHERE id = :id")
    fun observeById(id: Long): Flow<MonitoredPageEntity?>

    @Query("SELECT * FROM monitored_pages WHERE id = :id")
    suspend fun getById(id: Long): MonitoredPageEntity?

    @Query("SELECT * FROM monitored_pages WHERE enabled = 1")
    suspend fun getEnabled(): List<MonitoredPageEntity>

    @Insert
    suspend fun insert(page: MonitoredPageEntity): Long

    @Update
    suspend fun update(page: MonitoredPageEntity)

    @Delete
    suspend fun delete(page: MonitoredPageEntity)

    @Query(
        "UPDATE monitored_pages SET lastCheckedAt = :checkedAt, lastCheckStatus = :status, " +
            "lastErrorMessage = :errorMessage WHERE id = :id",
    )
    suspend fun updateCheckResult(id: Long, checkedAt: Long, status: CheckStatus, errorMessage: String?)
}
