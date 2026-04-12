package crontab

import utils.CronFactory
import java.io.StringReader
import java.io.StringWriter
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class CrontabManagerTest {
    @Test
    fun testItCanAddCrontabEntry() {
        val cronId = UUID.randomUUID()

        val cronEntry = CronEntry(
            CronFactory.createSimple(),
            "test-command",
            cronId
        )

        val testReader = StringReader("")
        val testWriter = StringWriter()

        val crontabManager = CrontabManager(testReader, testWriter)
        crontabManager.add(cronEntry)

        assertEquals(cronEntry.toFormattedLine(), testWriter.toString())
    }
}