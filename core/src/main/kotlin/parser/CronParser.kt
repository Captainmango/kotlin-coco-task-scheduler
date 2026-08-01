package parser

import kotlin.math.min
import parser.domain.Cron
import parser.domain.CronNode
import parser.domain.Interval

class CronParser
private constructor(
    input: String = "",
    private var currPos: Int = 0,
    private var readPos: Int = 0,
    private var currToken: Char = ' ',
    private val intervalOrder: List<Interval> = listOf(
        Interval.MINUTE,
        Interval.HOUR,
        Interval.DAY_OF_MONTH,
        Interval.MONTH,
        Interval.DAY_OF_WEEK,
    )
) {
    var input: String = input
        set(newValue) {
            field = newValue
            this.currPos = 0
            this.readPos = 0
        }

    companion object {
        fun make(input: String): CronParser {
            val parser = CronParser(input)
            parser.currToken = parser.input[parser.currPos]
            return parser
        }
    }

    fun parse(): Cron {
        val cronNodes = mutableListOf<CronNode>()
        while (this.currPos <= this.input.length - 1) {
            this.currToken = this.getToken()

            val cf =
                when {
                    this.currToken == '*' -> this.handleAsterisk()
                    this.currToken.isDigit() -> this.handleDigit()
                    this.currToken.isWhitespace() -> {
                        this.skipWhitespace()
                        this.advanceReadPos()

                        continue
                    }
                    // Should never get here
                    else -> error("Invalid input: " + this.currToken)
                }

            cronNodes.add(cf)

            this.advanceReadPos() // Advance to the whitespace
            this.syncReadAndCurrentPositions() // Sync the read and cur pos on the whitespace
            this.skipWhitespace() // skip it until next fragment
        }

        val intervalAnnotatedCronNodes =
            this.intervalOrder.zip(cronNodes).map { ( interval, cN ) ->
                cN.interval = interval
                cN
            }

        return Cron.fromMutableList(intervalAnnotatedCronNodes)
    }

    private fun advanceReadPos(pos: Int = 1) {
        this.readPos += pos
    }

    private fun syncReadAndCurrentPositions() {
        this.currPos = this.readPos
    }

    private fun getToken(): Char {
        return this.input[this.readPos]
    }

    private fun peekToken(): Char {
        return this.input[min(this.input.length - 1, this.readPos + 1)]
    }

    private fun skipWhitespace() {
        while (this.currPos < this.input.length && this.input[this.currPos].isWhitespace()) {
            this.currPos++
        }

        this.syncReadAndCurrentPositions()
    }

    private fun handleAsterisk(): CronNode {
        if (this.peekToken() == '/') {
            this.advanceReadPos(pos = 2) // move past forward slash
            val divisor = this.readNumber()
            return CronNode.Divisor(
                this.getRawStringFrom(this.currPos),
                div = divisor,
            )
        }

        // If the next char is whitespace or end of input, it's a wildcard
        if (this.peekToken().isWhitespace() || this.currPos == this.input.length - 1) {
            this.advanceReadPos()
            return CronNode.Wildcard("*")
        }

        error("Invalid fragment after asterisk: " + this.peekToken())
    }

    private fun handleDigit(): CronNode {
        val num = this.readNumber()

        // Check the next token at readPos (where readNumber stopped)
        val nextTok = if (this.readPos < this.input.length) this.input[this.readPos] else ' '

        return when (nextTok) {
            '-' -> {
                this.advanceReadPos() // skip '-'
                val end = this.readNumber()
                CronNode.Range(
                    this.getRawStringFrom(this.currPos),
                    start = num,
                    end = end,
                )
            }
            ',' -> {
                this.advanceReadPos() // skip ','
                val nextNum = this.readNumber()
                CronNode.NumList(
                    this.getRawStringFrom(this.currPos),
                    nums = listOf<Int>(num, nextNum),
                )
            }
            else -> {
                CronNode.Single(this.getRawStringFrom(this.currPos), num = num)
            }
        }
    }

    private fun readNumber(): Int {
        val col = mutableListOf<Char>()

        while (this.readPos < this.input.length && this.input[this.readPos].isDigit()) {
            col.add(this.input[this.readPos])
            this.advanceReadPos()
        }

        return String(col.toCharArray()).toInt()
    }

    private fun getRawStringFrom(startPos: Int): String {
        return this.input.slice(startPos until min(this.readPos, this.input.length))
    }
}
