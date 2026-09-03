package com.wafflehq.monitoring.data.monitoring

import com.wafflehq.monitoring.background.Notifier
import com.wafflehq.monitoring.background.needsRenotify
import com.wafflehq.monitoring.data.db.CheckResultEntity
import com.wafflehq.monitoring.data.db.MonitoredPageEntity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Clock
import javax.inject.Inject

class MonitorCheckUseCase @Inject constructor(
    private val pagesRepository: MonitoredPagesRepository,
    private val resultsRepository: CheckResultsRepository,
    private val httpClient: MonitorHttpClient,
    private val notifier: Notifier,
    private val clock: Clock,
) {

    suspend fun runChecks() {
        coroutineScope {
            pagesRepository.getEnabled()
                .map { page -> async { checkPage(page) } }
                .awaitAll()
        }
        renotifyOutstanding()
    }

    private suspend fun checkPage(page: MonitoredPageEntity) {
        val now = clock.millis()
        val body = httpClient.get(page.url).getOrElse { error ->
            pagesRepository.updateCheckResult(page.id, now, CheckStatus.ERROR, error.message)
            return
        }

        val matches = try {
            JsonPathEvaluator.evaluate(Json.parseToJsonElement(body), page.jsonPath)
        } catch (e: SerializationException) {
            pagesRepository.updateCheckResult(page.id, now, CheckStatus.ERROR, e.message)
            return
        }

        if (matches.isEmpty()) {
            pagesRepository.updateCheckResult(page.id, now, CheckStatus.OK_NO_MATCH, null)
            return
        }

        pagesRepository.updateCheckResult(page.id, now, CheckStatus.OK_MATCH, null)

        val result = CheckResultEntity(
            pageId = page.id,
            triggeredAt = now,
            matchedSummary = matches.take(MAX_SUMMARY_ITEMS).joinToString(
                prefix = "[",
                postfix = "]",
            ) { it.toString() }.take(MAX_SUMMARY_LENGTH),
            matchCount = matches.size,
            acknowledged = false,
            lastNotifiedAt = now,
        )
        val resultId = resultsRepository.insert(result)
        notifier.notifyTriggered(page, result.copy(id = resultId))
    }

    private suspend fun renotifyOutstanding() {
        val now = clock.millis()
        val due = resultsRepository.getAllUnacknowledged()
            .filter { needsRenotify(now, it.lastNotifiedAt, it.acknowledged) }
            .groupBy { it.pageId }

        for ((pageId, results) in due) {
            val page = pagesRepository.getById(pageId) ?: continue
            val latest = results.maxBy { it.triggeredAt }
            notifier.notifyTriggered(page, latest)
            resultsRepository.updateLastNotifiedAt(results.map { it.id }, now)
        }
    }

    private companion object {
        const val MAX_SUMMARY_ITEMS = 20
        const val MAX_SUMMARY_LENGTH = 2000
    }
}
