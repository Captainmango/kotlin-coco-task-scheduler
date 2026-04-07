package parser.domain

data class CronNode(
    val raw: String,
    val operands: List<Int>,
    val operator: Operator,
    val interval: Interval,
) {
    fun getPossibleValues(): List<Int> {
        return this.operator.opFn(this)
    }
}
