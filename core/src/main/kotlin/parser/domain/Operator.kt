package parser.domain

enum class Operator {
    WILDCARD,
    SINGLE,
    RANGE,
    LIST,
    DIVISOR,
}

fun wildcard(node: CronNode.Wildcard): List<Int> {
    val range = node.interval.min..node.interval.max
    return range.toList()
}

fun single(node: CronNode.Single): List<Int> {
    return listOf(node.num)
}

fun range(node: CronNode.Range): List<Int> {
    return (node.start..node.end).toList()
}

fun list(node: CronNode.NumList): List<Int> {
    return node.nums.sorted()
}

fun divisor(node: CronNode.Divisor): List<Int> {
    return (node.interval.min..node.interval.max).filter { it % node.div == 0 }.toList()
}
