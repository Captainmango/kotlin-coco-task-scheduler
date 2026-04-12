package utils

import parser.domain.Cron
import parser.domain.CronNode
import parser.domain.Interval

object CronFactory {
    fun createEmpty(): Cron {
        return Cron()
    }

    fun createSimple(): Cron {
        return Cron(
            CronNode.Wildcard("*", Interval.MINUTE),
            CronNode.Wildcard("*", Interval.HOUR),
            CronNode.Wildcard("*", Interval.DAY_OF_MONTH),
            CronNode.Wildcard("*", Interval.MONTH),
            CronNode.Wildcard("*", Interval.DAY_OF_WEEK),
        )
    }
}
