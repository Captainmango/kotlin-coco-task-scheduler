package parser.domain

enum class Interval(val min: Int, val max: Int) {
    UNKNOWN(0, 0),
    MINUTE(0, 59),
    HOUR(0, 23),
    DAY_OF_MONTH(1, 31),
    MONTH(1, 12),
    DAY_OF_WEEK(0, 6),
}
