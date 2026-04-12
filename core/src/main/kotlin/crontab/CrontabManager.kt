package crontab

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.UUID

const val CRON_ENTRY_LINE_FORMAT = "%s root /app/%s 2>&1 | tee -a /tmp/log # %s"

class CrontabManager(
    private val filePath: Path,
) {
    fun add(cronEntries: List<CronEntry>) {
        Files.newBufferedWriter(
            filePath,
            StandardOpenOption.WRITE,
            StandardOpenOption.APPEND,
            StandardOpenOption.SYNC,
        ).use { writer ->
            cronEntries.forEach { cE ->
                writer.appendLine(cE.toFormattedLine())
            }
        }
    }

    fun list(): List<CronEntry> {
        val out = mutableListOf<CronEntry>()
        Files.newBufferedReader(filePath).use { reader ->
            reader.lines().forEach { out.add(CronEntry.fromString(it)) }
        }
        return out
    }

    fun find(id: UUID): CronEntry = this.list().first { it.id == id }


    fun delete(id: UUID) {
        val writeBackList = this.list()
            .filter { it.id != id }
            .fold("") { acc, entry ->
                acc + entry.toFormattedLine() + "\n"
            }

        Files.newBufferedWriter(
            filePath,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.SYNC,
        ).use { writer ->
            writer.write(writeBackList)
        }
    }
}