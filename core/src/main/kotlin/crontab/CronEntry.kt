package crontab

import parser.CronParser
import parser.domain.Cron
import java.util.UUID

data class CronEntry(
    val cron: Cron,
    val cmd: String,
    var id: UUID = UUID.randomUUID(),
) {
    companion object {
        fun fromString(str: String): CronEntry {
            val (cronStr, rest) = str.split(" root ")

            val cron = CronParser.make(cronStr).parse()

            val (cmd, id) = rest.split(" # ")
            val uuid = UUID.fromString(id.slice(0 .. 35))

            val cmdRegex = """/app/(.+?) 2>&1""".toRegex()
            val match = cmdRegex.find(cmd)
            val extractedCmd = match?.groupValues?.get(1) ?: cmd

            return CronEntry(
                cron,
                extractedCmd,
                uuid,
            )
        }
    }

    fun toFormattedLine(): String {
        return String.format(
            CRON_ENTRY_LINE_FORMAT,
            this.cron.toString(),
            this.cmd,
            this.id.toString()
        )
    }
}
