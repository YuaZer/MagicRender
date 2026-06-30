package io.github.yuazer.magicrender.client.effect.trajectory

import io.github.yuazer.magicrender.config.BeamComponent
import io.github.yuazer.magicrender.config.TrailComponent
import io.github.yuazer.magicrender.config.TrailMotionFormulaComponent

enum class TrailRenderMode {
    FACE_CAMERA,
    WORLD_UP
}

enum class EffectBlendMode {
    ALPHA,
    ADDITIVE
}

data class TrailMotionDefinition(
    val mode: TrailMotionMode,
    val radius: Double,
    val angularSpeedDegreesPerTick: Double,
    val verticalAmplitude: Double,
    val verticalSpeedDegreesPerTick: Double,
    val phaseDegrees: Double,
    val formula: TrailMotionFormulaComponent
)

enum class TrailMotionMode {
    FOLLOW,
    ORBIT,
    HELIX,
    FORMULA
}

data class TrailDefinition(
    val effectId: String,
    val style: String,
    val texture: String,
    val maxPoints: Int,
    val lifetimeTicks: Int,
    val minSampleDistance: Double,
    val maxSegmentLength: Double,
    val maxInsertedPointsPerTick: Int,
    val sampleEveryTick: Boolean,
    val width: LinearCurve,
    val alpha: LinearCurve,
    val color: ColorGradient,
    val renderMode: TrailRenderMode,
    val blendMode: EffectBlendMode,
    val motion: TrailMotionDefinition
) {
    companion object {
        fun from(effectId: String, component: TrailComponent): TrailDefinition {
            return TrailDefinition(
                effectId = effectId,
                style = component.style,
                texture = component.texture,
                maxPoints = component.maxPoints,
                lifetimeTicks = component.lifetimeTicks,
                minSampleDistance = component.minSampleDistance,
                maxSegmentLength = component.maxSegmentLength,
                maxInsertedPointsPerTick = component.maxInsertedPointsPerTick,
                sampleEveryTick = component.sampleEveryTick,
                width = LinearCurve(component.widthStart, component.widthEnd),
                alpha = LinearCurve(1.0, 0.0),
                color = ColorGradient.fromHex(component.colorStart, component.colorEnd),
                renderMode = parseRenderMode(component.renderMode),
                blendMode = parseBlendMode(component.blendMode),
                motion = TrailMotionDefinition(
                    mode = parseMotionMode(component.motion.mode),
                    radius = component.motion.radius,
                    angularSpeedDegreesPerTick = component.motion.angularSpeed,
                    verticalAmplitude = component.motion.verticalAmplitude,
                    verticalSpeedDegreesPerTick = component.motion.verticalSpeed,
                    phaseDegrees = component.motion.phase,
                    formula = component.motion.formula
                )
            )
        }

        private fun parseRenderMode(value: String): TrailRenderMode {
            return when (value.lowercase()) {
                "world_up", "worldup" -> TrailRenderMode.WORLD_UP
                else -> TrailRenderMode.FACE_CAMERA
            }
        }

        fun parseBlendMode(value: String): EffectBlendMode {
            return when (value.lowercase()) {
                "alpha", "translucent" -> EffectBlendMode.ALPHA
                else -> EffectBlendMode.ADDITIVE
            }
        }

        private fun parseMotionMode(value: String): TrailMotionMode {
            return when (value.lowercase()) {
                "orbit" -> TrailMotionMode.ORBIT
                "helix", "spiral" -> TrailMotionMode.HELIX
                "formula", "js", "javascript" -> TrailMotionMode.FORMULA
                else -> TrailMotionMode.FOLLOW
            }
        }
    }
}

data class BeamDefinition(
    val effectId: String,
    val style: String,
    val texture: String,
    val width: LinearCurve,
    val color: ColorGradient,
    val segments: Int,
    val noise: Double,
    val renderMode: TrailRenderMode = TrailRenderMode.FACE_CAMERA,
    val blendMode: EffectBlendMode = EffectBlendMode.ADDITIVE
) {
    companion object {
        fun from(effectId: String, component: BeamComponent): BeamDefinition {
            return BeamDefinition(
                effectId = effectId,
                style = component.style,
                texture = component.texture,
                width = LinearCurve(component.width, component.width),
                color = ColorGradient.fromHex(component.colorStart, component.colorEnd),
                segments = component.segments,
                noise = component.noise,
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }
    }
}
