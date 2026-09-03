package com.wafflehq.monitoring.background

import org.junit.Assert.assertEquals
import org.junit.Test

class AlarmSchedulerTest {

    @Test
    fun `next trigger is thirty minutes after now by default`() {
        val now = 1_000_000L
        assertEquals(now + CHECK_INTERVAL_MILLIS, nextTriggerAtMillis(now))
    }

    @Test
    fun `check interval is exactly thirty minutes`() {
        assertEquals(30L * 60L * 1000L, CHECK_INTERVAL_MILLIS)
    }

    @Test
    fun `custom interval is honored`() {
        val now = 5_000L
        val customInterval = 60_000L
        assertEquals(now + customInterval, nextTriggerAtMillis(now, customInterval))
    }
}
