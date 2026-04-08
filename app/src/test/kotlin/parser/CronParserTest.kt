package parser

import parser.domain.CronNode
import parser.domain.Interval
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CronParserTest {
    @Test
    fun testCanCreateParser() {
        val expectedInput = "*"
        val parser = CronParser.make(expectedInput)

        assertEquals(expectedInput, parser.input)
    }

    @Test
    fun testCanReadTokens() {
        val expectedInput = "* 1 2-3"
        val parser = CronParser.make(expectedInput)

        val toks = parser.parse()

        val minuteFragment = CronNode.Wildcard("*", Interval.MINUTE)

        assertEquals(minuteFragment, toks.minute)
    }
}