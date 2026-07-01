package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.world.phys.Vec3
import kotlin.math.sin

object BeamPointGenerator {
    fun generate(instance: BeamEffectInstance): List<Vec3> {
        val context = TrajectoryRenderContext(
            cameraPosition = Vec3.ZERO,
            tickDelta = 1.0f,
            renderTimeTicks = instance.ageTicks.toDouble(),
            nowNanos = System.nanoTime()
        )
        return generate(instance, context)
    }

    fun generate(instance: BeamEffectInstance, context: TrajectoryRenderContext): List<Vec3> {
        val from = TrailAnchorResolver.resolve(instance.from, context) ?: return emptyList()
        val to = TrailAnchorResolver.resolve(instance.to, context) ?: return emptyList()
        val segments = instance.definition.segments.coerceAtLeast(1)
        val points = ArrayList<Vec3>(segments + 1)
        val direction = to.subtract(from)
        val side = perpendicular(direction)
        val age = instance.ageTicks.toDouble() + context.tickDelta.toDouble()

        for (index in 0..segments) {
            val t = index.toDouble() / segments.toDouble()
            val base = from.add(direction.scale(t))
            val offset = if (index == 0 || index == segments || instance.definition.noise <= 0.0) {
                Vec3(0.0, 0.0, 0.0)
            } else {
                val wave = sin((age + index * 13).toDouble() * 0.55)
                side.scale(wave * instance.definition.noise)
            }
            points += base.add(offset)
        }

        return points
    }

    private fun perpendicular(direction: Vec3): Vec3 {
        val normalized = if (direction.length() < 1.0E-5) Vec3(0.0, 1.0, 0.0) else direction.normalize()
        val up = Vec3(0.0, 1.0, 0.0)
        val side = normalized.cross(up)
        return if (side.length() < 1.0E-5) Vec3(1.0, 0.0, 0.0) else side.normalize()
    }
}
