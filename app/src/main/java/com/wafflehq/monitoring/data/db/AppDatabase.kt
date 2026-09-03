package com.wafflehq.monitoring.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MonitoredPageEntity::class, CheckResultEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun monitoredPageDao(): MonitoredPageDao
    abstract fun checkResultDao(): CheckResultDao
}
