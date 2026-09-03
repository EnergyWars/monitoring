package com.wafflehq.monitoring.background

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

const val ALARM_REQUEST_CODE = 1001
const val CHECK_INTERVAL_MINUTES = 30L
const val CHECK_INTERVAL_MILLIS = CHECK_INTERVAL_MINUTES * 60_000L

fun nextTriggerAtMillis(now: Long, intervalMillis: Long = CHECK_INTERVAL_MILLIS): Long = now + intervalMillis

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    private fun pendingIntent(flags: Int): PendingIntent? = PendingIntent.getBroadcast(
        context,
        ALARM_REQUEST_CODE,
        Intent(context, MonitorAlarmReceiver::class.java),
        flags,
    )

    fun scheduleNext() {
        val triggerAt = nextTriggerAtMillis(clock.millis())
        val pi = pendingIntent(PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE) ?: return
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
    }

    fun ensureScheduled() {
        val existing = pendingIntent(PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
        if (existing == null) scheduleNext()
    }

    fun cancel() {
        pendingIntent(PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)?.let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    fun canScheduleExactAlarms(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
}
