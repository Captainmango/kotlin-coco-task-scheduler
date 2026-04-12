package parser.domain

sealed interface CronNode {
    val raw: String
    val interval: Interval?

    data class Single(override val raw: String, override val interval: Interval, val num: Int) :
        CronNode

    data class Wildcard(override val raw: String, override val interval: Interval) : CronNode

    data class Range(
        override val raw: String,
        override val interval: Interval,
        val start: Int,
        val end: Int,
    ) : CronNode

    data class Divisor(override val raw: String, override val interval: Interval, val div: Int) :
        CronNode

    data class NumList(
        override val raw: String,
        override val interval: Interval,
        val nums: List<Int>,
    ) : CronNode

    fun getPossibleValues(): List<Int> {
        return when (this) {
            is Divisor -> divisor(this)
            is NumList -> list(this)
            is Range -> range(this)
            is Single -> single(this)
            is Wildcard -> wildcard(this)
        }
    }
}
