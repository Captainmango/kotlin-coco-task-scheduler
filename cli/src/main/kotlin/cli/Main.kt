package cli

import cli.application.Coco
import cli.application.ParseCron
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = Coco()
        .subcommands(ParseCron())
        .main(args)