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
        val world = Minecraft.getInstance().level
        val timeTicks = world?.gameTime?.toDouble() ?: 0.0
        return resolve(anchor, tickDelta = 1.0f, timeTicks = timeTicks)
    }

    fun resolve(anchor: TrailAnchor, context: TrajectoryRenderContext): Vec3? {
        return resolve(anchor, context.tickDelta, context.renderTimeTicks)
    }

    fun resolve(anchor: TrailAnchor, tickDelta: Float, timeTicks: Double): Vec3? {
        return when (anchor) {
            is TrailAnchor.Entity -> {
                val world = Minecraft.getInstance().level ?: return null
                val entity = world.getEntity(anchor.entityId) ?: return null
                interpolatedPosition(entity, tickDelta).add(anchor.offset)
            }
            is TrailAnchor.EntityMotion -> {
                val world = Minecraft.getInstance().level ?: return null
                val entity = world.getEntity(anchor.entityId) ?: return null
                val base = interpolatedPosition(entity, tickDelta).add(anchor.offset)
                base.add(computeMotionOffset(anchor.motion, timeTicks))
            }
            is TrailAnchor.WorldPoint -> anchor.position
        }
    }

    private fun interpolatedPosition(entity: net.minecraft.world.entity.Entity, tickDelta: Float): Vec3 {
        val t = tickDelta.toDouble().coerceIn(0.0, 1.0)
        return Vec3(
            entity.xOld + (entity.x - entity.xOld) * t,
            entity.yOld + (entity.y - entity.yOld) * t,
            entity.zOld + (entity.z - entity.zOld) * t
        )
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
