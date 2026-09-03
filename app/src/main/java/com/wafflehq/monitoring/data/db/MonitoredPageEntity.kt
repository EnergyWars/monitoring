package com.wafflehq.monitoring.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wafflehq.monitoring.data.monitoring.CheckStatus
import com.wafflehq.monitoring.data.monitoring.PageType

@Entity(tableName = "monitored_pages")
data class MonitoredPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: PageType,
    val url: String,
    val jsonPath: String,
    val enabled: Boolean,
    val createdAt: Long,
    val lastCheckedAt: Long?,
    val lastCheckStatus: CheckStatus,
    val lastErrorMessage: String?,
)
