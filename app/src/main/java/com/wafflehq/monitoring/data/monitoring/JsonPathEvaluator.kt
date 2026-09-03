package com.wafflehq.monitoring.data.monitoring

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Minimal JSON path evaluator. Segments are separated by ".", a segment
 * suffixed with "[*]" iterates a JSON array. Example: "a[*].b" returns the
 * flattened "b" values of every element in array "a".
 */
object JsonPathEvaluator {

    fun evaluate(root: JsonElement, path: String): List<JsonElement> {
        val segments = path.split(".").filter { it.isNotBlank() }
        var current: List<JsonElement> = listOf(root)

        for (segment in segments) {
            val wildcard = segment.endsWith("[*]")
            val key = if (wildcard) segment.removeSuffix("[*]") else segment

            current = current.flatMap { node ->
                val value = (node as? JsonObject)?.get(key) ?: return@flatMap emptyList()
                if (wildcard) {
                    (value as? JsonArray) ?: emptyList()
                } else {
                    listOf(value)
                }
            }
        }

        return current
    }
}
