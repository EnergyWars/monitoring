package com.wafflehq.monitoring.data.monitoring

class FakeMonitorHttpClient : MonitorHttpClient {
    private val responses = mutableMapOf<String, Result<String>>()

    fun respondWith(url: String, body: String) {
        responses[url] = Result.success(body)
    }

    fun failWith(url: String, message: String) {
        responses[url] = Result.failure(RuntimeException(message))
    }

    override suspend fun get(url: String): Result<String> =
        responses[url] ?: Result.failure(IllegalStateException("No fake response for $url"))
}
