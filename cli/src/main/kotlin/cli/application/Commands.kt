package cli.application

import cli.presentation.CronTableFormatter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import parser.CronParser

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