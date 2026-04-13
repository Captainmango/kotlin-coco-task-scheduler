package parser.domain

import jdk.internal.joptsimple.internal.Strings

enum class Operator {
    WILDCARD,
    SINGLE,
    RANGE,
    LIST,
    DIVISOR,
}

fun wildcard(node: CronNode.Wildcard): List<Int> {
    validateNode(node)
    val range = node.interval.min..node.interval.max
    return range.toList()
}

fun single(node: CronNode.Single): List<Int> {
    validateNode(node)
    return listOf(node.num)
}

fun range(node: CronNode.Range): List<Int> {
    validateNode(node)
    return (node.start..node.end).toList()
}

fun list(node: CronNode.NumList): List<Int> {
    validateNode(node)
    return node.nums.sorted()
}

fun divisor(node: CronNode.Divisor): List<Int> {
    validateNode(node)
    return (node.interval.min..node.interval.max).filter { it % node.div == 0 }.toList()
}

private fun validateNode(node: CronNode) {
    val valueIsAllowed: (n: Int) -> Boolean = { n: Int ->
        n >= node.interval!!.min && n <= node.interval!!.max
    }

    val errorFmtFn: (n: Int, i: Interval) -> String = { n: Int, i: Interval ->
        String.format("Value $n outside of ${i.name} range")
    }

    when (node) {
        is CronNode.Divisor -> {
            if (!valueIsAllowed(node.div)) error(errorFmtFn(node.div, node.interval))
        }
        is CronNode.NumList -> {
            node.nums.forEach { n ->
                if (!valueIsAllowed(n)) error(errorFmtFn(n, node.interval))
            }
        }
        is CronNode.Range -> {
            if (!valueIsAllowed(node.start)) error(errorFmtFn(node.start, node.interval))
            if (!valueIsAllowed(node.end)) error(errorFmtFn(node.end, node.interval))
        }
        is CronNode.Single -> if (!valueIsAllowed(node.num)) error(errorFmtFn(node.num, node.interval))
        is CronNode.Wildcard -> Unit // No validation needed on wildcard
    }
}
