package com.wafflehq.monitoring.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wafflehq.monitoring.data.monitoring.CheckResultsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationAckReceiver : BroadcastReceiver() {

    @Inject lateinit var checkResultsRepository: CheckResultsRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_ACKNOWLEDGE) return
        val pageId = intent.getLongExtra(EXTRA_PAGE_ID, -1L)
        if (pageId < 0) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                checkResultsRepository.acknowledgeAllForPage(pageId)
                notificationHelper.cancel(pageId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
