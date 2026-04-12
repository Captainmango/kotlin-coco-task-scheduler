package crontab

import config.Config
import utils.CronFactory
import java.io.*
import java.nio.file.Files
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import utils.createCrontabManager


class CrontabManagerTest {
    private var tempFile: java.nio.file.Path? = null

    @AfterTest
    fun cleanup() {
        this.tempFile?.let { Files.deleteIfExists(it) }
    }

    @Test
    fun testItCanAddCrontabEntry() {
        val cronId = UUID.randomUUID()

        val cronEntry = CronEntry(
            CronFactory.createSimple(),
            "test-command",
            cronId
        )

        val (crontabManager, f) = createCrontabManager()
        this.tempFile = f

        crontabManager.add(listOf(cronEntry))

        assertEquals(cronEntry.toFormattedLine() + "\n", Files.readString(f))
    }

    @Test
    fun testItCanListCrontabEntries() {
        val cronEntryOne = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val cronEntryTwo = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val content = """
            ${cronEntryOne.toFormattedLine()}
            ${cronEntryTwo.toFormattedLine()}
        """.trimIndent()


        val (crontabManager, f) = createCrontabManager(content)
        this.tempFile = f

        val results = crontabManager.list()

        assertContains(results, cronEntryOne)
        assertContains(results, cronEntryTwo)
    }

    @Test
    fun testItCanFindCrontabEntries() {
        val cronEntryOne = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val cronEntryTwo = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val content = """
            ${cronEntryOne.toFormattedLine()}
            ${cronEntryTwo.toFormattedLine()}
        """.trimIndent()

        val (crontabManager, f) = createCrontabManager(content)
        this.tempFile = f
        val result = crontabManager.find(cronEntryOne.id)

        assertEquals(cronEntryOne, result)
    }

    @Test
    fun testItCanDeleteCrontabEntry() {
        val cronEntryOne = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val cronEntryTwo = CronEntry(
            CronFactory.createSimple(),
            "test-command",
        )

        val content = """
            ${cronEntryOne.toFormattedLine()}
            ${cronEntryTwo.toFormattedLine()}
        """.trimIndent()

        val (crontabManager, f) = createCrontabManager(content)
        this.tempFile = f
        crontabManager.delete(cronEntryOne.id)

        val output = Files.readString(f)
        assertContains(output, cronEntryTwo.toFormattedLine())
        assert(!output.contains(cronEntryOne.toFormattedLine()))
    }

    @Test
    fun testItAppendsNewEntriesToEndOfExistingContent() {
        val existingEntry = CronEntry(
            CronFactory.createSimple(),
            "existing-command",
        )

        val newEntry = CronEntry(
            CronFactory.createSimple(),
            "new-command",
        )

        val content = existingEntry.toFormattedLine() + "\n"

        val (crontabManager, f) = createCrontabManager(content)
        this.tempFile = f

        crontabManager.add(listOf(newEntry))

        val output = Files.readString(f)
        val lines = output.lines().filter { it.isNotBlank() }

        assertEquals(2, lines.size)
        assertEquals(existingEntry.toFormattedLine(), lines[0])
        assertEquals(newEntry.toFormattedLine(), lines[1])
    }
}