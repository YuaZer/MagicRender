package io.github.yuazer.magicrender.client.effect.trajectory

import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

sealed interface TrailAnchor {
    data class Entity(val entityId: Int, val offset: Vec3 = Vec3(0.0, 0.0, 0.0)) : TrailAnchor
    data class EntityMotion(
        val entityId: Int,
        val offset: Vec3,
        val motion: TrailMotionDefinition
    ) : TrailAnchor
    data class WorldPoint(val position: Vec3) : TrailAnchor
}

object TrailAnchorResolver {
    fun resolve(anchor: TrailAnchor): Vec3? {
        return when (anchor) {
            is TrailAnchor.Entity -> {
                val world = Minecraft.getInstance().level ?: return null
                val entity = world.getEntity(anchor.entityId) ?: return null
                entity.position().add(anchor.offset)
            }
            is TrailAnchor.EntityMotion -> {
                val world = Minecraft.getInstance().level ?: return null
                val entity = world.getEntity(anchor.entityId) ?: return null
                val base = entity.position().add(anchor.offset)
                val tick = world.gameTime.toDouble()
                base.add(computeMotionOffset(anchor.motion, tick))
            }
            is TrailAnchor.WorldPoint -> anchor.position
        }
    }

    private fun computeMotionOffset(motion: TrailMotionDefinition, tick: Double): Vec3 {
        if (motion.mode == TrailMotionMode.FOLLOW) return Vec3.ZERO

        if (motion.mode == TrailMotionMode.FORMULA) {
            return computeFormulaOffset(motion, tick)
        }

        if (motion.radius <= 0.0) return Vec3.ZERO

        val angle = Math.toRadians(motion.phaseDegrees + tick * motion.angularSpeedDegreesPerTick)
        val x = cos(angle) * motion.radius
        val z = sin(angle) * motion.radius
        val y = when (motion.mode) {
            TrailMotionMode.HELIX -> {
                val verticalAngle = Math.toRadians(motion.phaseDegrees + tick * motion.verticalSpeedDegreesPerTick)
                sin(verticalAngle) * motion.verticalAmplitude
            }
            else -> 0.0
        }
        return Vec3(x, y, z)
    }

    private fun computeFormulaOffset(motion: TrailMotionDefinition, tick: Double): Vec3 {
        val angleDegrees = motion.phaseDegrees + tick * motion.angularSpeedDegreesPerTick
        val angle = Math.toRadians(angleDegrees)
        val verticalAngle = Math.toRadians(motion.phaseDegrees + tick * motion.verticalSpeedDegreesPerTick)
        val time = tick / 20.0
        val variables = mapOf(
            "tick" to tick,
            "time" to time,
            "radius" to motion.radius,
            "angularSpeed" to motion.angularSpeedDegreesPerTick,
            "verticalAmplitude" to motion.verticalAmplitude,
            "verticalSpeed" to motion.verticalSpeedDegreesPerTick,
            "phase" to motion.phaseDegrees,
            "angle" to angle,
            "angleDegrees" to angleDegrees,
            "verticalAngle" to verticalAngle
        )
        return Vec3(
            MotionFormulaEvaluator.evaluate(motion.formula.x, variables, 0.0),
            MotionFormulaEvaluator.evaluate(motion.formula.y, variables, 0.0),
            MotionFormulaEvaluator.evaluate(motion.formula.z, variables, 0.0)
        )
    }
}
