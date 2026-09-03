package com.wafflehq.monitoring.background

import kotlin.random.Random

/** Upper bound (inclusive) for the random delay applied before firing a check, in milliseconds. */
const val JITTER_MAX_MILLIS = 300_000L

fun randomJitterMillis(random: Random = Random.Default): Long = random.nextLong(0L, JITTER_MAX_MILLIS + 1)
