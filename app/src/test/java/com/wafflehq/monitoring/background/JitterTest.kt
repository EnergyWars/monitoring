package com.wafflehq.monitoring.background

import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class JitterTest {

    @Test
    fun `jitter is always within zero to three hundred seconds`() {
        repeat(1_000) {
            val jitter = randomJitterMillis()
            assertTrue(jitter in 0L..JITTER_MAX_MILLIS)
        }
    }

    @Test
    fun `jitter honors an injected random source`() {
        val alwaysZero = object : Random() {
            override fun nextBits(bitCount: Int): Int = 0
        }
        assertTrue(randomJitterMillis(alwaysZero) in 0L..JITTER_MAX_MILLIS)
    }
}
