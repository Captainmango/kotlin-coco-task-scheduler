package parser.domain

data class Cron (
    var minute: CronNode? = null,
    var hour: CronNode? = null,
    var dayOfMonth: CronNode? = null,
    var month: CronNode? = null,
    var dayOfWeek: CronNode? = null,
    var cmd: String? = null,
) {
    companion object {
        fun fromMutableList(l: List<CronNode>): Cron {
            val minuteFragment = l.getOrNull(0)
            val hourFragment = l.getOrNull(1)
            val dayOfMonthFragment = l.getOrNull(2)
            val monthFragment = l.getOrNull(3)
            val dayOfWeekFragment = l.getOrNull(4)

            return Cron(
                minuteFragment,
                hourFragment,
                dayOfMonthFragment,
                monthFragment,
                dayOfWeekFragment,
            )
        }
    }
}