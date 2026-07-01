package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3
import kotlin.math.floor

object TrailSampler {
    fun tick(instance: TrailEffectInstance) {
        if (!instance.state.enabled && instance.state.isEmpty()) return
    }

    fun sampleFrame(instance: TrailEffectInstance, context: TrajectoryRenderContext) {
        val state = instance.state
        val definition = instance.definition

        ageByTime(state, definition, context.nowNanos)

        if (!state.enabled) return
        val currentPosition = TrailAnchorResolver.resolve(instance.anchor, context) ?: run {
            state.enabled = false
            return
        }

        val last = state.lastSamplePosition
        if (last == null) {
            appendPoint(state, definition, currentPosition, context.nowNanos)
            state.lastSamplePosition = currentPosition
            return
        }

        val distance = last.distanceTo(currentPosition)
        if (distance < definition.minSampleDistance) {
            return
        }

        insertInterpolatedPoints(state, definition, last, currentPosition, distance, context.nowNanos)
        appendPoint(state, definition, currentPosition, context.nowNanos)
        state.lastSamplePosition = currentPosition
        trimToMaxPoints(state, definition.maxPoints)
    }

    private fun ageByTime(state: TrailState, definition: TrailDefinition, nowNanos: Long) {
        val lifetimeNanos = definition.lifetimeTicks.toLong().coerceAtLeast(1L) * 50_000_000L
        val aged = ArrayDeque<TrailPoint>()
        while (!state.points.isEmpty()) {
            val point = state.points.removeFirst()
            val ageNanos = nowNanos - point.spawnTimeNanos
            if (ageNanos <= lifetimeNanos) {
                val ageTicks = (ageNanos.toDouble() / 50_000_000.0).coerceAtLeast(point.ageTicks)
                aged.addLast(point.copy(ageTicks = ageTicks))
            }
        }
        state.points.addAll(aged)
        trimToMaxPoints(state, definition.maxPoints)
    }

    private fun insertInterpolatedPoints(
        state: TrailState,
        definition: TrailDefinition,
        from: Vec3,
        to: Vec3,
        distance: Double,
        nowNanos: Long
    ) {
        if (definition.maxInsertedPointsPerTick <= 0 || distance <= definition.maxSegmentLength) return
        val inserted = floor(distance / definition.maxSegmentLength)
            .toInt()
            .coerceAtMost(definition.maxInsertedPointsPerTick)
        for (index in 1..inserted) {
            val t = index.toDouble() / (inserted + 1).toDouble()
            appendPoint(state, definition, lerp(from, to, t), nowNanos)
        }
    }

    private fun appendPoint(state: TrailState, definition: TrailDefinition, position: Vec3, nowNanos: Long) {
        state.points.addLast(
            TrailPoint(
                position = position,
                ageTicks = 0.0,
                spawnTimeNanos = nowNanos,
                width = definition.width.evaluate(0.0),
                colorArgb = definition.color.evaluate(0.0)
            )
        )
        trimToMaxPoints(state, definition.maxPoints)
    }

    private fun trimToMaxPoints(state: TrailState, maxPoints: Int) {
        while (state.points.size > maxPoints) {
            state.points.removeFirst()
        }
    }

    private fun lerp(from: Vec3, to: Vec3, t: Double): Vec3 {
        return Vec3(
            from.x + (to.x - from.x) * t,
            from.y + (to.y - from.y) * t,
            from.z + (to.z - from.z) * t
        )
    }
}
