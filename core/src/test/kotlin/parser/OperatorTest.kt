package parser

import kotlin.test.Test
import kotlin.test.assertEquals
import parser.domain.CronNode
import parser.domain.Interval

class OperatorTest {
    @Test
    fun testWildCardOperator() {
        val expectedRange = (Interval.HOUR.min..Interval.HOUR.max).toList()
        val cronNode = CronNode.Wildcard("", Interval.HOUR)

        val res = cronNode.getPossibleValues()
        assertEquals(expectedRange, res)
    }

    @Test
    fun testRangeOperator() {
        val expectedRange = (1..5).toList()
        val cronNode = CronNode.Range("", Interval.HOUR, 1, 5)

        val res = cronNode.getPossibleValues()
        assertEquals(expectedRange, res)
    }

    @Test
    fun testListOperator() {
        val expectedRange = listOf(1, 3, 5)
        val cronNode = CronNode.NumList("", Interval.HOUR, listOf<Int>(3, 1, 5))

        val res = cronNode.getPossibleValues()
        assertEquals(expectedRange, res)
    }

    @Test
    fun testSingleOperator() {
        val expectedRange = listOf(4)
        val cronNode = CronNode.Single("", Interval.HOUR, 4)

        val res = cronNode.getPossibleValues()
        assertEquals(expectedRange, res)
    }

    @Test
    fun testDivisorOperator() {
        val expectedRange = listOf(0, 5, 10, 15, 20)
        val cronNode = CronNode.Divisor("", Interval.HOUR, 5)

        val res = cronNode.getPossibleValues()
        assertEquals(expectedRange, res)
    }
}
