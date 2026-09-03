package com.wafflehq.monitoring.background

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationHelperLogicTest {

    @Test
    fun `acknowledged result never needs renotify`() {
        assertFalse(needsRenotify(now = 10_000_000L, lastNotifiedAt = 0L, acknowledged = true))
    }

    @Test
    fun `unacknowledged result under one hour does not need renotify`() {
        val now = 1_000_000L
        val lastNotifiedAt = now - (RENOTIFY_INTERVAL_MILLIS - 1)
        assertFalse(needsRenotify(now, lastNotifiedAt, acknowledged = false))
    }

    @Test
    fun `unacknowledged result exactly one hour old needs renotify`() {
        val now = 1_000_000L
        val lastNotifiedAt = now - RENOTIFY_INTERVAL_MILLIS
        assertTrue(needsRenotify(now, lastNotifiedAt, acknowledged = false))
    }

    @Test
    fun `unacknowledged result well over one hour old needs renotify`() {
        val now = 10_000_000L
        val lastNotifiedAt = now - RENOTIFY_INTERVAL_MILLIS * 3
        assertTrue(needsRenotify(now, lastNotifiedAt, acknowledged = false))
    }
}
