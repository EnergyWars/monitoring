package com.wafflehq.monitoring.background

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ReliabilityStatus(
    val notificationsGranted: Boolean,
    val batteryOptimizationIgnored: Boolean,
    val exactAlarmsAllowed: Boolean,
    val showAutostart: Boolean,
) {
    val isFullyConfigured: Boolean
        get() = notificationsGranted && batteryOptimizationIgnored && exactAlarmsAllowed
}

private val XIAOMI_MANUFACTURERS = listOf("xiaomi", "redmi", "poco", "blackshark")

@Singleton
class ReliabilityChecker @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val alarmScheduler: AlarmScheduler,
) {
    fun status(): ReliabilityStatus {
        val powerManager = context.getSystemService(PowerManager::class.java)
        return ReliabilityStatus(
            notificationsGranted = NotificationManagerCompat.from(context).areNotificationsEnabled(),
            batteryOptimizationIgnored = powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: true,
            exactAlarmsAllowed = alarmScheduler.canScheduleExactAlarms(),
            showAutostart = isXiaomiFamily(),
        )
    }

    private fun isXiaomiFamily(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return XIAOMI_MANUFACTURERS.any { manufacturer.contains(it) }
    }
}
