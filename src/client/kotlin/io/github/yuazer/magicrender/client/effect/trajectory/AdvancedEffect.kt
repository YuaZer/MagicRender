package io.github.yuazer.magicrender.client.effect.trajectory

import io.github.yuazer.magicrender.config.AdvancedVisualComponent
import io.github.yuazer.magicrender.config.BloomApproximationComponent
import io.github.yuazer.magicrender.config.CircleLayerComponent
import io.github.yuazer.magicrender.config.CoreGlowComponent
import io.github.yuazer.magicrender.config.GlowPostComponent
import io.github.yuazer.magicrender.config.ParticleEmitterComponent
import io.github.yuazer.magicrender.config.RadialBurstComponent
import io.github.yuazer.magicrender.config.RibbonBundleComponent
import net.minecraft.world.phys.Vec3

data class AdvancedEffectDefinition(
    val effectId: String,
    val bloom: BloomApproximationDefinition,
    val glow: GlowPostDefinition,
    val core: CoreGlowDefinition,
    val particleEmitters: List<ParticleEmitterDefinition>,
    val ribbonBundles: List<RibbonBundleDefinition>,
    val circleLayers: List<CircleLayerDefinition>,
    val radialBursts: List<RadialBurstDefinition>
) {
    companion object {
        fun from(effectId: String, component: AdvancedVisualComponent): AdvancedEffectDefinition {
            return AdvancedEffectDefinition(
                effectId = effectId,
                bloom = BloomApproximationDefinition.from(component.bloom),
                glow = GlowPostDefinition.from(component.glow),
                core = CoreGlowDefinition.from(component.core),
                particleEmitters = component.particleEmitters.filter { it.enabled }.map(ParticleEmitterDefinition::from),
                ribbonBundles = component.ribbonBundles.filter { it.enabled }.map(RibbonBundleDefinition::from),
                circleLayers = component.circleLayers.filter { it.enabled }.map(CircleLayerDefinition::from),
                radialBursts = component.radialBursts.filter { it.enabled }.map(RadialBurstDefinition::from)
            )
        }
    }
}

data class GlowPostDefinition(
    val enabled: Boolean,
    val intensity: Double,
    val radius: Double,
    val iterations: Int,
    val downsample: Int,
    val threshold: Double
) {
    companion object {
        fun from(component: GlowPostComponent): GlowPostDefinition {
            return GlowPostDefinition(
                enabled = component.enabled,
                intensity = component.intensity,
                radius = component.radius,
                iterations = component.iterations,
                downsample = component.downsample,
                threshold = component.threshold
            )
        }
    }
}

data class BloomApproximationDefinition(
    val enabled: Boolean,
    val layers: Int,
    val scaleStep: Double,
    val alphaFalloff: Double
) {
    companion object {
        fun from(component: BloomApproximationComponent): BloomApproximationDefinition {
            return BloomApproximationDefinition(component.enabled, component.layers, component.scaleStep, component.alphaFalloff)
        }
    }
}

data class CoreGlowDefinition(
    val enabled: Boolean,
    val colorArgb: Int,
    val radius: Double,
    val pulseAmplitude: Double,
    val pulseSpeed: Double,
    val texture: String,
    val blendMode: EffectBlendMode
) {
    companion object {
        fun from(component: CoreGlowComponent): CoreGlowDefinition {
            return CoreGlowDefinition(
                enabled = component.enabled,
                colorArgb = TrajectoryColor.parseArgb(component.color),
                radius = component.radius,
                pulseAmplitude = component.pulseAmplitude,
                pulseSpeed = component.pulseSpeed,
                texture = component.texture,
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }
    }
}

data class ParticleEmitterDefinition(
    val shape: String,
    val count: Int,
    val colorStart: Int,
    val colorEnd: Int,
    val sizeStart: Double,
    val sizeEnd: Double,
    val radius: Double,
    val height: Double,
    val speed: Double,
    val swirlSpeed: Double,
    val noise: Double,
    val texture: String,
    val blendMode: EffectBlendMode
) {
    companion object {
        fun from(component: ParticleEmitterComponent): ParticleEmitterDefinition {
            return ParticleEmitterDefinition(
                shape = component.shape,
                count = component.count,
                colorStart = TrajectoryColor.parseArgb(component.colorStart),
                colorEnd = TrajectoryColor.parseArgb(component.colorEnd),
                sizeStart = component.sizeStart,
                sizeEnd = component.sizeEnd,
                radius = component.radius,
                height = component.height,
                speed = component.speed,
                swirlSpeed = component.swirlSpeed,
                noise = component.noise,
                texture = component.texture,
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }
    }
}

data class RibbonBundleDefinition(
    val count: Int,
    val widthStart: Double,
    val widthEnd: Double,
    val colorStart: Int,
    val colorEnd: Int,
    val length: Double,
    val samples: Int,
    val phaseStep: Double,
    val amplitude: Double,
    val frequency: Double,
    val twist: Double,
    val flowSpeed: Double,
    val texture: String,
    val blendMode: EffectBlendMode
) {
    companion object {
        fun from(component: RibbonBundleComponent): RibbonBundleDefinition {
            return RibbonBundleDefinition(
                count = component.count,
                widthStart = component.widthStart,
                widthEnd = component.widthEnd,
                colorStart = TrajectoryColor.parseArgb(component.colorStart),
                colorEnd = TrajectoryColor.parseArgb(component.colorEnd),
                length = component.length,
                samples = component.samples,
                phaseStep = component.phaseStep,
                amplitude = component.amplitude,
                frequency = component.frequency,
                twist = component.twist,
                flowSpeed = component.flowSpeed,
                texture = component.texture,
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }
    }
}

data class CircleLayerDefinition(
    val radius: Double,
    val thickness: Double,
    val colorArgb: Int,
    val segments: Int,
    val rotationSpeed: Double,
    val glyphs: Int,
    val glyphMode: String,
    val facing: MagicCircleFacing,
    val blendMode: EffectBlendMode
) {
    companion object {
        fun from(component: CircleLayerComponent): CircleLayerDefinition {
            return CircleLayerDefinition(
                radius = component.radius,
                thickness = component.thickness,
                colorArgb = TrajectoryColor.parseArgb(component.color),
                segments = component.segments,
                rotationSpeed = component.rotationSpeed,
                glyphs = component.glyphs,
                glyphMode = component.glyphMode,
                facing = when (component.facing.lowercase()) {
                    "horizontal", "world_up", "ground" -> MagicCircleFacing.HORIZONTAL
                    else -> MagicCircleFacing.FACE_CAMERA
                },
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }
    }
}

data class RadialBurstDefinition(
    val rays: Int,
    val length: Double,
    val widthStart: Double,
    val widthEnd: Double,
    val colorStart: Int,
    val colorEnd: Int,
    val rotationSpeed: Double,
    val randomJitter: Double,
    val texture: String,
    val blendMode: EffectBlendMode
) {
    companion object {
        fun from(component: RadialBurstComponent): RadialBurstDefinition {
            return RadialBurstDefinition(
                rays = component.rays,
                length = component.length,
                widthStart = component.widthStart,
                widthEnd = component.widthEnd,
                colorStart = TrajectoryColor.parseArgb(component.colorStart),
                colorEnd = TrajectoryColor.parseArgb(component.colorEnd),
                rotationSpeed = component.rotationSpeed,
                randomJitter = component.randomJitter,
                texture = component.texture,
                blendMode = TrailDefinition.parseBlendMode(component.blendMode)
            )
        }
    }
}

data class AdvancedEffectInstance(
    val handle: Long,
    val definition: AdvancedEffectDefinition,
    val source: TrailAnchor,
    val target: TrailAnchor?,
    val lifetimeTicks: Int,
    val seeds: List<Double>,
    var ageTicks: Int = 0,
    var renderAgeTicks: Double = 0.0
) {
    fun isAlive(): Boolean = ageTicks <= lifetimeTicks
}

data class BillboardVertex(
    val position: Vec3,
    val colorArgb: Int,
    val u: Float,
    val v: Float
)

data class BillboardMesh(
    val texture: String,
    val blendMode: EffectBlendMode,
    val vertices: List<BillboardVertex>
)
