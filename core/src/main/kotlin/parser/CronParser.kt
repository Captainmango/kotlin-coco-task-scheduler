package parser

import parser.domain.Cron
import parser.domain.CronNode
import parser.domain.Interval
import kotlin.math.min

class CronParser private constructor(
    val input: String = "",
    private var currPos: Int = 0,
    private var readPos: Int = 0,
    private var currToken: Char = ' ',
    private var currInterval: Interval = Interval.UNKNOWN
) {
    companion object {
        fun make(input: String): CronParser {
            val parser = CronParser(input)
            parser.currToken = parser.input[parser.currPos]
            return parser
        }
    }

    fun parse(): Cron {
        val cronNodes = mutableListOf<CronNode>()
        val cronOrder = sequenceOf<Interval>(
            Interval.MINUTE,
            Interval.HOUR,
            Interval.DAY_OF_MONTH,
            Interval.MONTH,
            Interval.DAY_OF_WEEK,
        ).iterator()

        this.currInterval = cronOrder.next()

        while (this.currPos <= this.input.length - 1) {
            this.currToken = this.getToken()

            val cf = when {
                this.currToken == '*' -> this.handleAsterisk()
                this.currToken.isDigit() -> this.handleDigit()
                this.currToken.isWhitespace() -> {
                    this.advancePositions()
                    this.currToken = this.getToken()

                    if (this.currToken != '*' || !this.currToken.isDigit()) {
                        error("Invalid format: " + this.input)
                    }

                    continue
                }
                // Should never get here
                else -> error("Invalid input: " + this.currToken)
            }

            cronNodes.add(cf)

            this.skipWhitespace()

            if (cronOrder.hasNext()) {
                this.currInterval = cronOrder.next()
            }
        }

        return Cron.fromMutableList(cronNodes)
    }

    private fun advancePositions() {
        this.currPos++
        this.readPos = this.currPos
    }

    private fun skipWhitespace() {
        if (this.currPos < this.input.length && this.input[this.currPos].isWhitespace()) {
            this.currPos++
        }

        this.readPos = this.currPos
    }

    private fun getToken(): Char {
        return this.input[this.currPos]
    }

    private fun peekToken(): Char {
        return this.input[min(this.input.length - 1, this.currPos+1)]
    }

    private fun handleAsterisk(): CronNode {
        val startPos = this.currPos

        if (this.peekToken() == '/') {
            this.currPos += 2 // move past forward slash
            this.readPos = this.currPos

            val divisor = this.readNumber()
            this.currPos = this.readPos // advance currPos past the number

            return CronNode.Divisor(
                this.getRawStringFrom(startPos),
                this.currInterval,
                div = divisor
            )
        }

        // If the next char is whitespace or end of input, it's a wildcard
        if (this.peekToken().isWhitespace() || this.currPos == this.input.length - 1) {
            this.advancePositions()
            return CronNode.Wildcard("*", this.currInterval)
        }

        error("Invalid fragment after asterisk: " + this.peekToken())
    }

    private fun handleDigit(): CronNode {
        val startPos = this.currPos
        this.readPos = this.currPos

        val num = this.readNumber()

        // Check the next token at readPos (where readNumber stopped)
        val nextTok = if (this.readPos < this.input.length) this.input[this.readPos] else ' '

        return when (nextTok) {
            '-' -> {
                this.currPos = this.readPos + 1 // skip '-'
                this.readPos = this.currPos

                val end = this.readNumber()

                this.currPos = this.readPos // advance currPos past the range end
                CronNode.Range(
                    this.getRawStringFrom(startPos),
                    this.currInterval,
                    start = num,
                    end = end
                )
            }
            ',' -> {
                this.currPos = this.readPos + 1 // skip ','
                this.readPos = this.currPos

                val nextNum = this.readNumber()

                this.currPos = this.readPos // advance currPos past the list end
                CronNode.NumList(
                    this.getRawStringFrom(startPos),
                    this.currInterval,
                    listOf<Int>(
                        num,
                        nextNum,
                    )
                )
            }
            else -> {
                this.currPos = this.readPos
                CronNode.Single(
                    this.getRawStringFrom(startPos),
                    this.currInterval,
                    num = num
                )
            }
        }
    }

    private fun readNumber(): Int {
        val col = mutableListOf<Char>()

        while (this.readPos < this.input.length && this.input[this.readPos].isDigit()) {
            col.add(this.input[this.readPos])
            this.readPos += 1
        }

        return String(col.toCharArray()).toInt()
    }

    private fun getRawStringFrom(startPos: Int): String {
        return this.input.slice(startPos until min(this.readPos, this.input.length))
    }
}
