package com.wafflehq.monitoring.ui.format

import java.text.DateFormat
import java.util.Date

fun formatTimestamp(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))
