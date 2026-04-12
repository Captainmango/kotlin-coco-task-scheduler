package crontab

const val CRON_ENTRY_LINE_FORMAT = "%s root /app/%s 2>&1 | tee -a /tmp/log # %s\\n"

class CrontabManager(
    private val reader: java.io.Reader,
    private val writer: java.io.Writer,
) {
    fun add(cronEntry: CronEntry) {
        this.writer.use { f ->
            f.write(cronEntry.toFormattedLine())
        }
    }

//    fun list(): List<CronEntry> {
//        val out = mutableListOf<CronEntry>()
//
//        this.reader.use { f ->
//            f.readLines().forEach { CronEntry.fromString(it) }
//        }
//    }
}