package coco.cli

import parser.CronParser

fun main() {
    val parser = CronParser.make("1 1-5 */3 1,2 *")

    val out = parser.parse()

    print(out)
}