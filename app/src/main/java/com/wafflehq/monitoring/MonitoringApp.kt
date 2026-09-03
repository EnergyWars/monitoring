package com.wafflehq.monitoring

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wafflehq.monitoring.background.AlarmScheduler
import com.wafflehq.monitoring.background.CHECK_INTERVAL_MINUTES
import com.wafflehq.monitoring.background.MonitorCheckWorker
import com.wafflehq.monitoring.background.UNIQUE_WORK_NAME
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MonitoringApp : Application(), Configuration.Provider {

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory
    @Inject lateinit var alarmScheduler: AlarmScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(hiltWorkerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicFallback()
        alarmScheduler.ensureScheduled()
    }

    private fun schedulePeriodicFallback() {
        val request = PeriodicWorkRequestBuilder<MonitorCheckWorker>(
            CHECK_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
