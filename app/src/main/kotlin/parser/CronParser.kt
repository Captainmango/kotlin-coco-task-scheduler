package parser

import kotlin.math.min

class CronParser private constructor(
    val input: String,
    private var readPos: Int = 0,
    private var currToken: Char = ' ',
) {
    companion object {
        fun make(input: String): CronParser {
            val parser = CronParser(input)
            parser.currToken = parser.input[parser.readPos]
            return parser
        }
    }

    fun readToken(): Char {
        val tok = this.getToken()
        this.readPos += 1
        return tok
    }

    private fun getToken(): Char {
        return this.input[this.readPos]
    }

    private fun peekToken(): Char {
        return this.input[min(this.input.length, this.readPos + 1)]
    }
}