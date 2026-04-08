package parser

import parser.domain.Cron
import parser.domain.CronNode
import parser.domain.Interval
import kotlin.math.min

class CronParser private constructor(
    val input: String,
    private var currPos: Int = 0,
    private var readPos: Int = 0,
    private var currToken: Char = ' ',
) {
    companion object {
        fun make(input: String): CronParser {
            val parser = CronParser(input)
            parser.currToken = parser.input[parser.currPos]
            return parser
        }
    }

    fun parse(): Cron {
        this.currToken = this.getToken()
        val col = mutableListOf<CronNode>()

        while (this.currPos < this.input.length) {
            this.currToken = this.getToken()

            val t = '*' == this.currToken

            var cf = when {
                '*' == this.currToken -> this.handleAsterisk()
                else -> CronNode.Wildcard("", Interval.HOUR)
            }

            col.add(cf)

            this.currPos += 1
            this.readPos = this.currPos
        }

        return Cron.fromMutableList(col)
    }

    private fun getToken(): Char {
        return this.input[this.currPos]
    }

    private fun peekToken(): Char {
        return this.input[min(this.input.length - 1, this.readPos + 1)]
    }

    private fun handleAsterisk(): CronNode {
        if (this.peekToken() == ' ') {
            return CronNode.Wildcard(this.currToken.toString(), Interval.MINUTE)
        }

        if (this.peekToken() == '/') {
            // handle divisor
        }

        error("Invalid fragment")
    }
}