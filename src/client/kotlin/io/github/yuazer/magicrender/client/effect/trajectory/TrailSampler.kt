package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3
import kotlin.math.floor

object TrailSampler {
    fun tick(instance: TrailEffectInstance) {
        val state = instance.state
        val definition = instance.definition

        ageAndTrim(state, definition)

        if (!state.enabled) return
        val currentPosition = TrailAnchorResolver.resolve(instance.anchor) ?: run {
            state.enabled = false
            return
        }

        val last = state.lastSamplePosition
        if (last == null) {
            appendPoint(state, definition, currentPosition)
            state.lastSamplePosition = currentPosition
            return
        }

        val distance = last.distanceTo(currentPosition)
        if (!definition.sampleEveryTick && distance < definition.minSampleDistance) {
            return
        }

        insertInterpolatedPoints(state, definition, last, currentPosition, distance)
        appendPoint(state, definition, currentPosition)
        state.lastSamplePosition = currentPosition
        trimToMaxPoints(state, definition.maxPoints)
    }

    private fun ageAndTrim(state: TrailState, definition: TrailDefinition) {
        val aged = ArrayDeque<TrailPoint>()
        while (!state.points.isEmpty()) {
            val point = state.points.removeFirst()
            val nextAge = point.ageTicks + 1
            if (nextAge <= definition.lifetimeTicks) {
                aged.addLast(point.copy(ageTicks = nextAge))
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
        distance: Double
    ) {
        if (definition.maxInsertedPointsPerTick <= 0 || distance <= definition.maxSegmentLength) return
        val inserted = floor(distance / definition.maxSegmentLength)
            .toInt()
            .coerceAtMost(definition.maxInsertedPointsPerTick)
        for (index in 1..inserted) {
            val t = index.toDouble() / (inserted + 1).toDouble()
            appendPoint(state, definition, lerp(from, to, t))
        }
    }

    private fun appendPoint(state: TrailState, definition: TrailDefinition, position: Vec3) {
        state.points.addLast(
            TrailPoint(
                position = position,
                ageTicks = 0,
                spawnTimeNanos = System.nanoTime(),
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

