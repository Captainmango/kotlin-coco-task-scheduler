package parser

import kotlin.test.Test
import kotlin.test.assertEquals
import parser.domain.CronNode
import parser.domain.Interval

class IntervalTest {
    @Test
    fun testMinuteLowerBound() {
        val parser = CronParser.make("0 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("0", Interval.MINUTE, 0), cron.minute)
    }

    @Test
    fun testMinuteUpperBound() {
        val parser = CronParser.make("59 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("59", Interval.MINUTE, 59), cron.minute)
    }

    @Test
    fun testHourLowerBound() {
        val parser = CronParser.make("* 0 * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("0", Interval.HOUR, 0), cron.hour)
    }

    @Test
    fun testHourUpperBound() {
        val parser = CronParser.make("* 23 * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("23", Interval.HOUR, 23), cron.hour)
    }

    @Test
    fun testDayOfMonthLowerBound() {
        val parser = CronParser.make("* * 1 * *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("1", Interval.DAY_OF_MONTH, 1), cron.dayOfMonth)
    }

    @Test
    fun testDayOfMonthUpperBound() {
        val parser = CronParser.make("* * 31 * *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("31", Interval.DAY_OF_MONTH, 31), cron.dayOfMonth)
    }

    @Test
    fun testMonthLowerBound() {
        val parser = CronParser.make("* * * 1 *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("1", Interval.MONTH, 1), cron.month)
    }

    @Test
    fun testMonthUpperBound() {
        val parser = CronParser.make("* * * 12 *")
        val cron = parser.parse()
        assertEquals(CronNode.Single("12", Interval.MONTH, 12), cron.month)
    }

    @Test
    fun testDayOfWeekLowerBound() {
        val parser = CronParser.make("* * * * 0")
        val cron = parser.parse()
        assertEquals(CronNode.Single("0", Interval.DAY_OF_WEEK, 0), cron.dayOfWeek)
    }

    @Test
    fun testDayOfWeekUpperBound() {
        val parser = CronParser.make("* * * * 6")
        val cron = parser.parse()
        assertEquals(CronNode.Single("6", Interval.DAY_OF_WEEK, 6), cron.dayOfWeek)
    }

    @Test
    fun testRangeWithLowerBounds() {
        val parser = CronParser.make("0-5 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Range("0-5", Interval.MINUTE, 0, 5), cron.minute)
    }

    @Test
    fun testRangeWithUpperBounds() {
        val parser = CronParser.make("50-59 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Range("50-59", Interval.MINUTE, 50, 59), cron.minute)
    }

    @Test
    fun testDivisorWithLowerBound() {
        val parser = CronParser.make("*/1 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Divisor("*/1", Interval.MINUTE, 1), cron.minute)
    }

    @Test
    fun testDivisorWithUpperBound() {
        val parser = CronParser.make("*/30 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.Divisor("*/30", Interval.MINUTE, 30), cron.minute)
    }

    @Test
    fun testListWithBoundaries() {
        val parser = CronParser.make("0,59 * * * *")
        val cron = parser.parse()
        assertEquals(CronNode.NumList("0,59", Interval.MINUTE, listOf(0, 59)), cron.minute)
    }
}
