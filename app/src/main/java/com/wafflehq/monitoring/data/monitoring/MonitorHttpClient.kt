package com.wafflehq.monitoring.data.monitoring

interface MonitorHttpClient {
    suspend fun get(url: String): Result<String>
}
