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
                    val nextTok = this.peekToken()
                    if (nextTok.isWhitespace()) {
                        error("Invalid format. Too much whitespace")
                    }

                    this.advanceReadHead(step = 1)
                    continue
                }
                else -> error("Invalid input: " + this.currToken)
            }

            cronNodes.add(cf)

            this.advanceReadHead(until = ' ')
            if (cronOrder.hasNext()) {
                this.currInterval = cronOrder.next()
            }
        }

        return Cron.fromMutableList(cronNodes)
    }

    private fun advanceReadHead(step: Int = 1, until: Char? = null) {
        if (until != null) {
            for ((idx, c) in this.input.slice(this.currPos..this.input.length-1).withIndex()) {
                if (c == until) {
                    this.currPos = idx
                    this.readPos = this.currPos
                }
            }
        }

        this.currPos += step
        this.readPos = this.currPos
    }

    private fun getToken(): Char {
        return this.input[this.currPos]
    }

    private fun peekToken(): Char {
        return this.input[min(this.input.length - 1, this.readPos + 1)]
    }

    private fun handleAsterisk(): CronNode {
        if (this.peekToken() == '/') {
            this.readPos += 2 // advance past forward slash as currently on asterisk
            val divisor = String(this.readNumber()).toInt()

            return CronNode.Divisor(
                this.getRawString(),
                this.currInterval,
                div = divisor
            )
        }

        // If the last char is an asterisk then it has to be a wildcard fragment.
        if (this.currPos == this.readPos || this.peekToken() == ' ') {
            return CronNode.Wildcard(this.getRawString(), this.currInterval)
        }

        error("Invalid fragment")
    }

    private fun handleDigit(): CronNode {
        val numChars = this.readNumber()

        val nextTok = this.peekToken()

        return when (nextTok) {
            '-' -> {
                this.readPos += 1
                val topOfRange = this.readNumber()
                CronNode.Range(
                    this.getRawString(),
                    this.currInterval,
                    start = numChars.joinToString().toInt(),
                    end = topOfRange.joinToString().toInt()
                )
            }
            ',' -> {
                this.readPos += 1
                val nextNum = this.readNumber()
                CronNode.NumList(
                    this.getRawString(),
                    this.currInterval,
                    listOf<Int>(
                        numChars.joinToString().toInt(),
                        nextNum.joinToString().toInt(),
                    )
                )
            }
            else -> CronNode.Single(
                this.getRawString(),
                this.currInterval,
                num = String(numChars).toInt()
            )
        }
    }

    private fun readNumber(): CharArray {
        val col = mutableListOf<Char>()

        while (this.readPos < this.input.length && this.input[this.readPos].isDigit()) {
            col.add(this.input[this.readPos])
            this.readPos += 1
        }

        this.currPos = this.readPos
        return col.toCharArray()
    }

    private fun getRawString(): String {
        return this.input.slice(this.currPos..min(this.readPos, this.input.length-1))
    }
}