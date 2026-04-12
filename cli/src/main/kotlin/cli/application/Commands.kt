package cli.application

import cli.presentation.CronTableFormatter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import config.basePath
import config.config
import crontab.CronEntry
import crontab.CrontabManager
import parser.CronParser
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

class Coco : CliktCommand() {
    override fun run() = Unit
}

class ParseCron : CliktCommand(name = "parse-cron") {
    val cronExpr: String by argument(name = "cron-expression", help = "The cron expression to be parsed")
    override fun run() {
        val cron = CronParser.make(cronExpr).parse()
        echo(CronTableFormatter.format(cron))
    }
}

class AddTask : CliktCommand() {
    val cronExpr: String by argument(name = "cron-expression", help = "The cron expression")
    val cmd: String by argument(name = "command", help = "The command to be ran")
    override fun run() {
        val cronFile = File(config!!.crontabFile)
        if (!cronFile.exists()) {
            Files.createFile(cronFile.toPath())
        }

        val crontabManager = CrontabManager(cronFile.toPath())

        val cron = CronParser.make(cronExpr).parse()

        val cronEntry = CronEntry(cron, cmd)
        crontabManager.add(listOf(cronEntry))
    }
}