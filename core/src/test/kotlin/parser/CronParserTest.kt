package parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import parser.domain.CronNode
import parser.domain.Interval

class CronParserTest {
    @Test
    fun testCanCreateParser() {
        val expectedInput = "*"
        val parser = CronParser.make(expectedInput)

        assertEquals(expectedInput, parser.input)
    }

    @Test
    fun testCanParseBasicInput() {
        val expectedInput = "* 1 * * *"
        val parser = CronParser.make(expectedInput)

        val cron = parser.parse()

        val minuteFragment = CronNode.Wildcard("*", Interval.MINUTE)
        val hourFragment = CronNode.Single("1", Interval.HOUR, 1)

        assertEquals(minuteFragment, cron.minute)
        assertEquals(hourFragment, cron.hour)
    }

    @Test
    fun testCanParseFullBasicCron() {
        val expectedInput = "*/15 0-4 1,15 2 *"
        val parser = CronParser.make(expectedInput)

        val cron = parser.parse()

        val minuteFragment = CronNode.Divisor("*/15", Interval.MINUTE, 15)
        val hourFragment = CronNode.Range("0-4", Interval.HOUR, 0, 4)
        val dayOfMonthFragment = CronNode.NumList("1,15", Interval.DAY_OF_MONTH, listOf(1, 15))
        val monthFragment = CronNode.Single("2", Interval.MONTH, 2)
        val dayOfWeekFragment = CronNode.Wildcard("*", Interval.DAY_OF_WEEK)

        assertEquals(minuteFragment, cron.minute)
        assertEquals(hourFragment, cron.hour)
        assertEquals(dayOfMonthFragment, cron.dayOfMonth)
        assertEquals(monthFragment, cron.month)
        assertEquals(dayOfWeekFragment, cron.dayOfWeek)
    }

    @Test
    fun testEmptyInputThrowsError() {
        assertFailsWith<Exception> { CronParser.make("").parse() }
    }

    @Test
    fun testInvalidCharacterThrowsError() {
        assertFailsWith<Exception> { CronParser.make("a * * * *").parse() }
    }

    @Test
    fun testInvalidFragmentAfterAsteriskThrowsError() {
        assertFailsWith<Exception> { CronParser.make("*5 * * * *").parse() }
    }

    @Test
    fun testInvalidFormatWithWhitespaceThrowsError() {
        assertFailsWith<Exception> { CronParser.make("*  * * * *").parse() }
    }
}
