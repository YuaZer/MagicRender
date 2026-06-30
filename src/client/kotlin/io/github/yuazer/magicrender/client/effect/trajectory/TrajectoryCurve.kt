package io.github.yuazer.magicrender.client.effect.trajectory

data class LinearCurve(
    val start: Double,
    val end: Double
) {
    fun evaluate(t: Double): Double {
        val clamped = t.coerceIn(0.0, 1.0)
        return start + (end - start) * clamped
    }
}

