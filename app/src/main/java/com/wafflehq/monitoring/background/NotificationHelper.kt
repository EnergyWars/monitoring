package com.wafflehq.monitoring.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.wafflehq.monitoring.MainActivity
import com.wafflehq.monitoring.R
import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val EXTRA_OPEN_PAGE_ID = "open_page_id"
const val NOTIFICATION_CHANNEL_ID = "monitoring_matches"
const val ACTION_ACKNOWLEDGE = "com.wafflehq.monitoring.action.ACKNOWLEDGE"
const val EXTRA_PAGE_ID = "page_id"

/** One hour renotify interval for unacknowledged matches. */
const val RENOTIFY_INTERVAL_MILLIS = 60L * 60L * 1000L

fun needsRenotify(now: Long, lastNotifiedAt: Long, acknowledged: Boolean): Boolean =
    !acknowledged && now - lastNotifiedAt >= RENOTIFY_INTERVAL_MILLIS

@Singleton
class NotificationHelper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : Notifier {
    private val manager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.notification_channel_desc)
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    override fun notifyTriggered(page: MonitoredPageEntity, result: CheckResultEntity) {
        ensureChannel()

        val contentIntent = PendingIntent.getActivity(
            context,
            page.id.toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_PAGE_ID, page.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val ackIntent = PendingIntent.getBroadcast(
            context,
            page.id.toInt(),
            Intent(context, NotificationAckReceiver::class.java).apply {
                action = ACTION_ACKNOWLEDGE
                putExtra(EXTRA_PAGE_ID, page.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title, page.name))
            .setContentText(context.getString(R.string.notification_body, result.matchCount))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(contentIntent)
            .addAction(0, context.getString(R.string.notification_action_confirm), ackIntent)
            .build()

        manager.notify(page.id.toInt(), notification)
    }

    fun cancel(pageId: Long) {
        manager.cancel(pageId.toInt())
    }
}
