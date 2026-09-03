package com.wafflehq.monitoring.background

import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.data.db.MonitoredPageEntity

interface Notifier {
    fun notifyTriggered(page: MonitoredPageEntity, result: CheckResultEntity)
}
