package io.github.yuazer.magicrender.client.effect.trajectory

import io.github.yuazer.magicrender.config.MagicCircleComponent
import net.minecraft.world.phys.Vec3

enum class MagicCircleFacing {
    FACE_CAMERA,
    HORIZONTAL
}

data class MagicCircleDefinition(
    val effectId: String,
    val style: String,
    val radius: Double,
    val colorArgb: Int,
    val thickness: Double,
    val segments: Int,
    val facing: MagicCircleFacing,
    val rotationSpeedDegreesPerTick: Double,
    val innerRadiusScale: Double,
    val glyphs: Int,
    val blendMode: EffectBlendMode
) {
    companion object {
        fun from(effectId: String, component: MagicCircleComponent): MagicCircleDefinition {
            return MagicCircleDefinition(
                effectId = effectId,
                style = component.style,
                radius = component.radius,
                colorArgb = TrajectoryColor.parseArgb(component.color),
                thickness = component.thickness,
                segments = component.segments,
                facing = parseFacing(component.facing),
                rotationSpeedDegreesPerTick = component.rotationSpeed,
                innerRadiusScale = component.innerRadiusScale,
                glyphs = component.glyphs,
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }

        private fun parseFacing(value: String): MagicCircleFacing {
            return when (value.lowercase()) {
                "horizontal", "world_up", "ground" -> MagicCircleFacing.HORIZONTAL
                else -> MagicCircleFacing.FACE_CAMERA
            }
        }
    }
}

data class MagicCircleInstance(
    val handle: Long,
    val definition: MagicCircleDefinition,
    val anchor: TrailAnchor,
    val lifetimeTicks: Int,
    var ageTicks: Int = 0
) {
    fun isAlive(): Boolean = ageTicks <= lifetimeTicks
}

data class MagicCircleVertex(
    val position: Vec3,
    val colorArgb: Int
)

data class MagicCircleMesh(
    val blendMode: EffectBlendMode,
    val vertices: List<MagicCircleVertex>
)
