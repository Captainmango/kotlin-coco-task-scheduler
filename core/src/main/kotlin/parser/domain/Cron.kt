package parser.domain

data class Cron(
    var minute: CronNode? = null,
    var hour: CronNode? = null,
    var dayOfMonth: CronNode? = null,
    var month: CronNode? = null,
    var dayOfWeek: CronNode? = null,
) {
    companion object {
        fun fromMutableList(l: List<CronNode>): Cron {
            val minuteFragment = l.find { cN -> cN.interval == Interval.MINUTE }
            val hourFragment = l.find { cN -> cN.interval == Interval.HOUR }
            val dayOfMonthFragment = l.find { cN -> cN.interval == Interval.DAY_OF_MONTH }
            val monthFragment = l.find { cN -> cN.interval == Interval.MONTH }
            val dayOfWeekFragment = l.find { cN -> cN.interval == Interval.DAY_OF_WEEK }

            return Cron(
                minuteFragment,
                hourFragment,
                dayOfMonthFragment,
                monthFragment,
                dayOfWeekFragment,
            )
        }
    }

    override fun toString(): String {
        return String.format(
            "%s %s %s %s %s",
            this.minute?.raw,
            this.hour?.raw,
            this.dayOfMonth?.raw,
            this.month?.raw,
            this.dayOfWeek?.raw,
        )
    }
}
