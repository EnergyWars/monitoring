package com.wafflehq.monitoring.data.monitoring

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonPathEvaluatorTest {

    private val emptySlotsResponse = """
        {
          "availabilities": [
            { "date": "2026-09-03", "slots": [] },
            { "date": "2026-09-04", "slots": [] }
          ],
          "total": 0
        }
    """.trimIndent()

    private val filledSlotsResponse = """
        {
          "availabilities": [
            { "date": "2026-09-03", "slots": [] },
            { "date": "2026-09-04", "slots": ["2026-09-04T09:00:00", "2026-09-04T09:30:00"] }
          ],
          "total": 2
        }
    """.trimIndent()

    @Test
    fun `doctolib example with empty slots yields no matches`() {
        val root = Json.parseToJsonElement(emptySlotsResponse)
        val matches = JsonPathEvaluator.evaluate(root, "availabilities[*].slots[*]")
        assertTrue(matches.isEmpty())
    }

    @Test
    fun `doctolib example with filled slots yields matches`() {
        val root = Json.parseToJsonElement(filledSlotsResponse)
        val matches = JsonPathEvaluator.evaluate(root, "availabilities[*].slots[*]")
        assertEquals(2, matches.size)
    }

    @Test
    fun `missing path segment yields no matches`() {
        val root = Json.parseToJsonElement(emptySlotsResponse)
        val matches = JsonPathEvaluator.evaluate(root, "doesNotExist[*].slots[*]")
        assertTrue(matches.isEmpty())
    }

    @Test
    fun `non-array value under wildcard segment yields no matches`() {
        val root = Json.parseToJsonElement("""{ "total": 0 }""")
        val matches = JsonPathEvaluator.evaluate(root, "total[*]")
        assertTrue(matches.isEmpty())
    }

    @Test
    fun `plain non-wildcard path returns single value`() {
        val root = Json.parseToJsonElement(emptySlotsResponse)
        val matches = JsonPathEvaluator.evaluate(root, "total")
        assertEquals(1, matches.size)
    }

    @Test
    fun `blank path returns the root element`() {
        val root = Json.parseToJsonElement("""{ "a": 1 }""")
        val matches = JsonPathEvaluator.evaluate(root, "")
        assertEquals(1, matches.size)
    }
}
