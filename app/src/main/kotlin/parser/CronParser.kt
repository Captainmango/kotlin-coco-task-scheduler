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
    private var currInterval: Interval
) {
    companion object {
        fun make(input: String): CronParser {
            val parser = CronParser(input, currInterval = Interval.MINUTE)
            parser.currToken = parser.input[parser.currPos]
            return parser
        }
    }

    fun parse(): Cron {
        this.currToken = this.getToken()
        val col = mutableListOf<CronNode>()
        val order = sequenceOf<Interval>(
            Interval.MINUTE,
            Interval.HOUR,
            Interval.DAY_OF_MONTH,
            Interval.MONTH,
            Interval.DAY_OF_WEEK,
        ).iterator()

        this.currInterval = order.next()

        while (this.currPos < this.input.length) {
            this.currToken = this.getToken()

            var cf = when {
                this.currToken == '*' -> this.handleAsterisk()
                this.currToken.isDigit() -> this.handleDigit()
                this.currToken.isWhitespace() -> {
                    val nextTok = this.peekToken()
                    if (nextTok.isWhitespace()) {
                        error("Invalid format. Too much whitespace")
                    }

                    this.currPos += 1
                    this.readPos = this.currPos
                    continue
                }
                else -> error("Invalid input: " + this.currToken)
            }

            col.add(cf)

            this.currPos += 1
            this.readPos = this.currPos
            this.currInterval = order.next()
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
            return CronNode.Wildcard(this.currToken.toString(), this.currInterval)
        }

        if (this.peekToken() == '/') {
            // handle divisor
        }

        error("Invalid fragment")
    }

    private fun handleDigit(): CronNode {
        val numChars = this.readNumber()

        val nextTok = this.peekToken()

        return when {
            nextTok.isWhitespace() -> CronNode.Single(
                this.input.slice(this.currPos..this.readPos),
                this.currInterval,
                num = numChars.contentToString().toInt()
            )
            else -> error("oops")
        }
    }

    private fun readNumber(): CharArray {
        var tok = this.getToken()
        val col = mutableListOf<Char>()

        while (tok.isDigit()) {
            col.add(tok)

            tok = this.peekToken()
            if (tok.isWhitespace()) {
                break
            }

            if (!tok.isDigit()) {
                break
            }

            this.readPos += 1
        }

        return col.toCharArray()
    }
}