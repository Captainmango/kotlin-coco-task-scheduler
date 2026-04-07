package parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CronParserTest {
    @Test
    fun testCanCreateParser() {
        val expectedInput = "*"
        val parser = CronParser.make(expectedInput)

        assertEquals(expectedInput, parser.input)
        assertEquals('*', parser.readToken())
    }

    @Test
    fun testCanReadTokens() {
        val expectedInput = "* 1 2-3"
        val parser = CronParser.make(expectedInput)

        val cur = parser.readToken()
        val next = parser.readToken()

        assertNotEquals(cur, next)
        assertEquals(expectedInput[0], cur)
        assertEquals(expectedInput[1], next)
    }
}