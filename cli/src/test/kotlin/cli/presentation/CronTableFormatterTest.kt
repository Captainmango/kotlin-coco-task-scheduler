package cli.presentation

import kotlin.test.Test
import kotlin.test.assertContains
import parser.CronParser

class CronTableFormatterTest {

    @Test
    fun testItOutputsCronInTableFormat() {
        val cron = CronParser.make("*/15 0 1,15 * 1-5").parse()
        val output = CronTableFormatter.format(cron)

        assertContains(output, "Field")
        assertContains(output, "Possible Values")
        assertContains(output, "minute")
        assertContains(output, "hour")
        assertContains(output, "day of month")
        assertContains(output, "month")
        assertContains(output, "day of week")
        assertContains(output, "0 15 30 45")
        assertContains(output, "1 2 3 4 5")
    }
}
