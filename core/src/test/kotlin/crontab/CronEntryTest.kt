package crontab

import utils.CronFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class CronEntryTest {
    @Test
    fun testItRendersCorrectLineFormat() {
        val cronEntry = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val lineFormattedCronEntry = String.format(
            CRON_ENTRY_LINE_FORMAT,
            cronEntry.cron.toString(),
            cronEntry.cmd,
            cronEntry.id.toString(),
        )

        assertEquals(lineFormattedCronEntry, cronEntry.toFormattedLine())
    }

    @Test
    fun testItCorrectlyRendersEmptyCron() {
        val cronEntry = CronEntry(
            CronFactory.createEmpty(),
            "test-command",
        )

        val lineFormattedCronEntry = String.format(
            CRON_ENTRY_LINE_FORMAT,
            cronEntry.cron.toString(), // Shows up as an empty string
            cronEntry.cmd,
            cronEntry.id.toString(),
        )

        assertEquals(lineFormattedCronEntry, cronEntry.toFormattedLine())
    }

    @Test
    fun testCanCreateAndReverseFromString() {
        val cronEntry = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val stringRepr = cronEntry.toFormattedLine()
        val cronEntryFromString = CronEntry.fromString(stringRepr)

        val cronEntryStringRepr = cronEntryFromString.toFormattedLine()
        val cronEntryReversedToAndFromString = CronEntry.fromString(cronEntryStringRepr)

        assertEquals(cronEntryFromString, cronEntryReversedToAndFromString)
    }
}