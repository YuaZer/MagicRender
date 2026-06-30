package io.github.yuazer.magicrender.client.effect.trajectory

import kotlin.math.roundToInt

data class ColorGradient(
    val startArgb: Int,
    val endArgb: Int
) {
    fun evaluate(t: Double): Int {
        val clamped = t.coerceIn(0.0, 1.0)
        val sa = startArgb ushr 24 and 0xFF
        val sr = startArgb ushr 16 and 0xFF
        val sg = startArgb ushr 8 and 0xFF
        val sb = startArgb and 0xFF
        val ea = endArgb ushr 24 and 0xFF
        val er = endArgb ushr 16 and 0xFF
        val eg = endArgb ushr 8 and 0xFF
        val eb = endArgb and 0xFF
        val a = lerp(sa, ea, clamped)
        val r = lerp(sr, er, clamped)
        val g = lerp(sg, eg, clamped)
        val b = lerp(sb, eb, clamped)
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    companion object {
        val WHITE = ColorGradient(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt())

        fun fromHex(start: String, end: String): ColorGradient {
            return ColorGradient(parseHexColor(start), parseHexColor(end))
        }

        private fun parseHexColor(value: String): Int {
            val raw = value.removePrefix("#")
            val argb = when (raw.length) {
                6 -> "FF$raw"
                8 -> raw
                else -> "FFFFFFFF"
            }
            val a = argb.substring(0, 2).toInt(16)
            val r = argb.substring(2, 4).toInt(16)
            val g = argb.substring(4, 6).toInt(16)
            val b = argb.substring(6, 8).toInt(16)
            return a shl 24 or (r shl 16) or (g shl 8) or b
        }

        private fun lerp(start: Int, end: Int, t: Double): Int {
            return (start + (end - start) * t).roundToInt().coerceIn(0, 255)
        }
    }
}

object TrajectoryColor {
    fun parseArgb(value: String): Int {
        return ColorGradient.fromHex(value, value).startArgb
    }
}
