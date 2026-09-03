package com.wafflehq.monitoring.background

import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.data.db.MonitoredPageEntity

class FakeNotifier : Notifier {
    val notifications = mutableListOf<Pair<MonitoredPageEntity, CheckResultEntity>>()

    override fun notifyTriggered(page: MonitoredPageEntity, result: CheckResultEntity) {
        notifications += page to result
    }
}
