package crontab

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.Reader
import java.io.Writer
import java.util.UUID

const val CRON_ENTRY_LINE_FORMAT = "%s root /app/%s 2>&1 | tee -a /tmp/log # %s"

class CrontabManager(
    reader: Reader,
    writer: Writer,
) : AutoCloseable {
    private val bufferedReader = BufferedReader(reader)
    private val bufferedWriter = BufferedWriter(writer)

    fun add(cronEntries: List<CronEntry>) {
        cronEntries.forEach { cE ->
            this.bufferedWriter.appendLine(cE.toFormattedLine())
        }
        this.bufferedWriter.flush()
    }

    fun list(): List<CronEntry> {
        val out = mutableListOf<CronEntry>()
        this.bufferedReader.lines().forEach { out.add(CronEntry.fromString(it)) }
        return out
    }

    fun find(id: UUID): CronEntry? {
        return this.list().first { it.id == id }
    }

    fun delete(id: UUID) {
        val writeBackList = this.list()
            .filter { it.id != id }
            .fold("") { acc, entry ->
                acc + entry.toFormattedLine() + "\n"
            }

        this.bufferedWriter.write(writeBackList)
        this.bufferedWriter.flush()
    }

    override fun close() {
        bufferedReader.close()
        bufferedWriter.close()
    }
}