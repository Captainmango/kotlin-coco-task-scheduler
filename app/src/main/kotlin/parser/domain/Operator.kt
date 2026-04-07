package parser.domain

enum class Operator(val opFn: (n: CronNode) -> List<Int>) {
    WILDCARD({ wildcard(it) }),
    SINGLE({ single(it) }),
    RANGE({ range(it) }),
    LIST({ list(it) }),
    DIVISOR({ divisor(it) }),
}

fun wildcard(node: CronNode): List<Int> {
    val range = node.interval.min..node.interval.max
    return range.toList()
}

fun single(node: CronNode): List<Int> {
    return listOf(node.operands[0])
}

fun range(node: CronNode): List<Int> {
    val one = node.operands[0]
    val two = node.operands[1]
    return (one..two).toList()
}

fun list(node: CronNode): List<Int> {
    return node.operands.sorted()
}

fun divisor(node: CronNode): List<Int> {
    val operand = node.operands[0]
    return (node.interval.min..node.interval.max).filter{ it % operand == 0 }.toList()
}