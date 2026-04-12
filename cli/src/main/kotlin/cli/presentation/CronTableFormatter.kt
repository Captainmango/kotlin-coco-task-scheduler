package cli.presentation

import parser.domain.Cron
import parser.domain.CronNode

object CronTableFormatter {

    private const val FIELD_WIDTH = 12
    private const val VALUES_WIDTH = 60

    fun format(cron: Cron): String {
        val lines = mutableListOf<String>()

        // Header
        lines.add("${"Field".padEnd(FIELD_WIDTH)} | ${"Possible Values".padEnd(VALUES_WIDTH)}")
        lines.add("${"-".repeat(FIELD_WIDTH)}-+${"-".repeat(VALUES_WIDTH + 1)}")

        // Rows
        cron.minute?.let { lines.add(formatRow("minute", it)) }
        cron.hour?.let { lines.add(formatRow("hour", it)) }
        cron.dayOfMonth?.let { lines.add(formatRow("day of month", it)) }
        cron.month?.let { lines.add(formatRow("month", it)) }
        cron.dayOfWeek?.let { lines.add(formatRow("day of week", it)) }

        return lines.joinToString("\n")
    }

    private fun formatRow(fieldName: String, node: CronNode): String {
        val values = node.getPossibleValues().joinToString(" ")
        return "${fieldName.padEnd(FIELD_WIDTH)} | $values"
    }
}
