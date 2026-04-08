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
    fun testCanParseBasicInput() {
        val expectedInput = "* 1"
        val parser = CronParser.make(expectedInput)

        val cron = parser.parse()

        val minuteFragment = CronNode.Wildcard("*", Interval.MINUTE)
        val hourFragment = CronNode.Single("1", Interval.HOUR, 1)

        assertEquals(minuteFragment, cron.minute)
        assertEquals(hourFragment, cron.hour)
    }

    @Test
    fun testCanParseFullBasicCron() {
        val expectedInput = "*/15 1-5 1,15 * 1"
        val parser = CronParser.make(expectedInput)

        val cron = parser.parse()

        val minuteFragment = CronNode.Wildcard("*", Interval.MINUTE)
        val hourFragment = CronNode.Wildcard("*", Interval.HOUR)
        val dayOfMonthFragment = CronNode.Wildcard("*", Interval.DAY_OF_MONTH)
        val monthFragment = CronNode.Wildcard("*", Interval.MONTH)
        val dayOfWeekFragment = CronNode.Wildcard("*", Interval.DAY_OF_WEEK)

        assertEquals(minuteFragment, cron.minute)
        assertEquals(hourFragment, cron.hour)
        assertEquals(dayOfMonthFragment, cron.dayOfMonth)
        assertEquals(monthFragment, cron.month)
        assertEquals(dayOfWeekFragment, cron.dayOfWeek)
    }
}