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
            return Cron(
                l[0],
                l[1],
                l[2],
                l[3],
                l[4],
            )
        }
    }
}