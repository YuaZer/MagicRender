package io.github.yuazer.magicrender.client.effect.trajectory

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object MotionFormulaEvaluator {
    fun evaluate(expression: String, variables: Map<String, Double>, fallback: Double): Double {
        val source = expression.trim()
        if (source.isEmpty()) return fallback
        return try {
            Parser(source, variables).parse()
        } catch (_: Exception) {
            fallback
        }
    }

    private class Parser(
        private val source: String,
        private val variables: Map<String, Double>
    ) {
        private var index = 0

        fun parse(): Double {
            val value = parseExpression()
            skipWhitespace()
            if (index != source.length) error("Unexpected token")
            return value
        }

        private fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                value = when {
                    match('+') -> value + parseTerm()
                    match('-') -> value - parseTerm()
                    else -> return value
                }
            }
        }

        private fun parseTerm(): Double {
            var value = parsePower()
            while (true) {
                skipWhitespace()
                value = when {
                    match('*') -> value * parsePower()
                    match('/') -> value / parsePower()
                    match('%') -> value % parsePower()
                    else -> return value
                }
            }
        }

        private fun parsePower(): Double {
            var value = parseUnary()
            skipWhitespace()
            if (match('^')) {
                value = value.pow(parsePower())
            }
            return value
        }

        private fun parseUnary(): Double {
            skipWhitespace()
            return when {
                match('+') -> parseUnary()
                match('-') -> -parseUnary()
                else -> parsePrimary()
            }
        }

        private fun parsePrimary(): Double {
            skipWhitespace()
            if (match('(')) {
                val value = parseExpression()
                expect(')')
                return value
            }
            if (peek()?.isDigit() == true || peek() == '.') return parseNumber()
            if (peek()?.isLetter() == true || peek() == '_') return parseIdentifier()
            error("Expected value")
        }

        private fun parseIdentifier(): Double {
            val name = readIdentifier()
            val normalized = name.removePrefix("Math.")
            skipWhitespace()
            if (match('(')) {
                val args = mutableListOf<Double>()
                skipWhitespace()
                if (!match(')')) {
                    do {
                        args += parseExpression()
                        skipWhitespace()
                    } while (match(','))
                    expect(')')
                }
                return call(normalized, args)
            }
            return when (normalized) {
                "PI" -> PI
                "E" -> kotlin.math.E
                else -> variables[normalized] ?: error("Unknown variable")
            }
        }

        private fun call(name: String, args: List<Double>): Double {
            fun one(block: (Double) -> Double): Double {
                if (args.size != 1) error("Expected one argument")
                return block(args[0])
            }
            fun two(block: (Double, Double) -> Double): Double {
                if (args.size != 2) error("Expected two arguments")
                return block(args[0], args[1])
            }
            return when (name) {
                "sin" -> one(::sin)
                "cos" -> one(::cos)
                "tan" -> one(::tan)
                "asin" -> one(::asin)
                "acos" -> one(::acos)
                "atan" -> one(::atan)
                "atan2" -> two(::atan2)
                "sqrt" -> one(::sqrt)
                "abs" -> one(::abs)
                "min" -> two(::min)
                "max" -> two(::max)
                "pow" -> two { base, exponent -> base.pow(exponent) }
                "log" -> one(::ln)
                "exp" -> one(::exp)
                "floor" -> one(::floor)
                "ceil" -> one(::ceil)
                else -> error("Unknown function")
            }
        }

        private fun parseNumber(): Double {
            val start = index
            while (peek()?.isDigit() == true || peek() == '.') index++
            if (peek() == 'e' || peek() == 'E') {
                index++
                if (peek() == '+' || peek() == '-') index++
                while (peek()?.isDigit() == true) index++
            }
            return source.substring(start, index).toDouble()
        }

        private fun readIdentifier(): String {
            val start = index
            while (peek()?.isLetterOrDigit() == true || peek() == '_' || peek() == '.') index++
            return source.substring(start, index)
        }

        private fun skipWhitespace() {
            while (peek()?.isWhitespace() == true) index++
        }

        private fun match(char: Char): Boolean {
            if (peek() != char) return false
            index++
            return true
        }

        private fun expect(char: Char) {
            if (!match(char)) error("Expected $char")
        }

        private fun peek(): Char? = source.getOrNull(index)
    }
}
