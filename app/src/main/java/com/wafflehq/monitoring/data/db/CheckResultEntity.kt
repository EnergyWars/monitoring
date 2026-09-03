package com.wafflehq.monitoring.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "check_results",
    foreignKeys = [
        ForeignKey(
            entity = MonitoredPageEntity::class,
            parentColumns = ["id"],
            childColumns = ["pageId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("pageId")],
)
data class CheckResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pageId: Long,
    val triggeredAt: Long,
    val matchedSummary: String,
    val matchCount: Int,
    val acknowledged: Boolean,
    val lastNotifiedAt: Long,
)
