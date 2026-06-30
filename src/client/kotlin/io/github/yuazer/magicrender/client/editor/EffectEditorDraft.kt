package io.github.yuazer.magicrender.client.editor

import com.google.gson.JsonObject
import io.github.yuazer.magicrender.config.BeamComponent
import io.github.yuazer.magicrender.config.AdvancedVisualComponent
import io.github.yuazer.magicrender.config.BloomApproximationComponent
import io.github.yuazer.magicrender.config.CircleLayerComponent
import io.github.yuazer.magicrender.config.CoreGlowComponent
import io.github.yuazer.magicrender.config.EffectComponents
import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.EffectVisibility
import io.github.yuazer.magicrender.config.MagicCircleComponent
import io.github.yuazer.magicrender.config.ParticleEmitterComponent
import io.github.yuazer.magicrender.config.RadialBurstComponent
import io.github.yuazer.magicrender.config.RibbonBundleComponent
import io.github.yuazer.magicrender.config.TrailComponent
import io.github.yuazer.magicrender.config.TrailMotionComponent
import io.github.yuazer.magicrender.config.TrailMotionFormulaComponent

data class EffectEditorDraft(
    var version: Int = 1,
    var id: String = "magicrender:editor_arcane_stream",
    var enabled: Boolean = true,
    var group: String = "default",
    var durationTicks: Int = 120,
    var importance: String = "normal",
    var visibility: VisibilityDraft = VisibilityDraft(),
    var components: ComponentDrafts = ComponentDrafts(),
    var preview: PreviewDraft = PreviewDraft()
) {
    fun toEffectDefinition(): EffectDefinition {
        return EffectDefinition(
            version = version,
            id = id,
            enabled = enabled,
            group = group.ifBlank { "default" },
            durationTicks = durationTicks,
            importance = importance.ifBlank { "normal" },
            visibility = EffectVisibility(
                drawDistance = visibility.drawDistance,
                hideWhenShadersConflict = visibility.hideWhenShadersConflict
            ),
            components = EffectComponents(
                magicCircle = components.magicCircle.toComponent(),
                beam = components.beam.toComponent(),
                trail = components.trail.toComponent(),
                advanced = components.advanced.toComponent()
            )
        )
    }

    fun toJsonObject(): JsonObject {
        return EffectEditorJson.toJsonObject(toEffectDefinition())
    }

    companion object {
        fun entityArcaneStream(): EffectEditorDraft {
            return EffectEditorDraft(
                id = "magicrender:editor_arcane_stream",
                durationTicks = 160,
                visibility = VisibilityDraft(drawDistance = 96, hideWhenShadersConflict = false),
                components = ComponentDrafts(
                    trail = TrailDraft(
                        enabled = true,
                        widthStart = 0.32,
                        widthEnd = 0.02,
                        colorStart = "#FFFF66FF",
                        colorEnd = "#FF66FFFF",
                        lifetimeTicks = 44,
                        maxPoints = 72,
                        minSampleDistance = 0.03,
                        maxSegmentLength = 0.35,
                        motion = TrailMotionDraft(
                            mode = "helix",
                            radius = 0.7,
                            angularSpeed = 14.0,
                            verticalAmplitude = 0.22,
                            verticalSpeed = 6.0
                        )
                    ),
                    magicCircle = MagicCircleDraft(
                        enabled = true,
                        radius = 1.6,
                        color = "#FFD447FF",
                        thickness = 0.055,
                        facing = "face_camera",
                        rotationSpeed = 1.2,
                        glyphs = 14
                    ),
                    beam = BeamDraft(
                        enabled = true,
                        width = 0.12,
                        colorStart = "#FFFF88FF",
                        colorEnd = "#FF66FFFF",
                        segments = 18,
                        noise = 0.18
                    ),
                    advanced = AdvancedDraft(
                        enabled = true,
                        core = CoreGlowDraft(enabled = true, color = "#FF66FFFF", radius = 0.75, pulseAmplitude = 0.22),
                        particleEmitters = mutableListOf(
                            ParticleEmitterDraft(enabled = true, shape = "sphere", count = 180, colorStart = "#FF445CFF", colorEnd = "#AAFF44FF", sizeStart = 0.08, sizeEnd = 0.018, radius = 2.4, height = 2.2, noise = 0.35),
                            ParticleEmitterDraft(enabled = true, shape = "column", count = 120, colorStart = "#CCAA66FF", colorEnd = "#3366FFFF", sizeStart = 0.06, sizeEnd = 0.015, radius = 0.9, height = 4.2, speed = 0.04, swirlSpeed = 1.4)
                        ),
                        ribbonBundles = mutableListOf(
                            RibbonBundleDraft(enabled = true, count = 12, widthStart = 0.12, widthEnd = 0.018, colorStart = "#FFFFEEAA", colorEnd = "#FF66FF99", length = 8.0, samples = 120, phaseStep = 21.0, amplitude = 0.75, frequency = 1.5, twist = 0.6, flowSpeed = 0.12)
                        ),
                        circleLayers = mutableListOf(
                            CircleLayerDraft(enabled = true, radius = 2.1, thickness = 0.045, color = "#FFFFFF22", rotationSpeed = 1.0, glyphs = 18),
                            CircleLayerDraft(enabled = true, radius = 1.45, thickness = 0.03, color = "#CCFF66FF", rotationSpeed = -1.7, glyphs = 12)
                        ),
                        radialBursts = mutableListOf(
                            RadialBurstDraft(enabled = true, rays = 22, length = 2.8, widthStart = 0.09, widthEnd = 0.0, colorStart = "#FFFF66FF", colorEnd = "#00FF66FF", rotationSpeed = 0.45)
                        )
                    )
                )
            )
        }
    }
}

data class PreviewDraft(
    var targetMode: String = "fixed_distance",
    var fixedDistance: Double = 6.0,
    var sourceHeightOffset: Double = 0.55,
    var targetHeightOffset: Double = 0.55,
    var fallbackToFixedDistance: Boolean = true
)

data class VisibilityDraft(
    var drawDistance: Int = 96,
    var hideWhenShadersConflict: Boolean = false
)

data class ComponentDrafts(
    var trail: TrailDraft = TrailDraft(),
    var beam: BeamDraft = BeamDraft(),
    var magicCircle: MagicCircleDraft = MagicCircleDraft(),
    var advanced: AdvancedDraft = AdvancedDraft()
)

data class TrailDraft(
    var enabled: Boolean = true,
    var style: String = "ribbon",
    var widthStart: Double = 0.28,
    var widthEnd: Double = 0.0,
    var colorStart: String = "#FFFFFFFF",
    var colorEnd: String = "#FFFFFFFF",
    var lifetimeTicks: Int = 36,
    var maxPoints: Int = 64,
    var minSampleDistance: Double = 0.04,
    var maxSegmentLength: Double = 0.45,
    var maxInsertedPointsPerTick: Int = 4,
    var sampleEveryTick: Boolean = true,
    var renderMode: String = "face_camera",
    var texture: String = "minecraft:textures/particle/flame.png",
    var blendMode: String = "additive",
    var motion: TrailMotionDraft = TrailMotionDraft()
) {
    fun toComponent(): TrailComponent {
        return TrailComponent(
            enabled = enabled,
            style = style.ifBlank { "ribbon" },
            width = widthStart,
            color = colorStart,
            colorStart = colorStart,
            colorEnd = colorEnd,
            widthStart = widthStart,
            widthEnd = widthEnd,
            lifetimeTicks = lifetimeTicks,
            maxPoints = maxPoints,
            minSampleDistance = minSampleDistance,
            maxSegmentLength = maxSegmentLength,
            maxInsertedPointsPerTick = maxInsertedPointsPerTick,
            sampleEveryTick = sampleEveryTick,
            renderMode = renderMode.ifBlank { "face_camera" },
            texture = texture.ifBlank { "minecraft:textures/particle/flame.png" },
            blendMode = blendMode.ifBlank { "additive" },
            motion = motion.toComponent()
        )
    }
}

data class TrailMotionDraft(
    var mode: String = "follow",
    var radius: Double = 0.0,
    var angularSpeed: Double = 0.0,
    var verticalAmplitude: Double = 0.0,
    var verticalSpeed: Double = 0.0,
    var phase: Double = 0.0,
    var formula: TrailMotionFormulaDraft = TrailMotionFormulaDraft()
) {
    fun toComponent(): TrailMotionComponent {
        return TrailMotionComponent(
            mode = mode.ifBlank { "follow" },
            radius = radius,
            angularSpeed = angularSpeed,
            verticalAmplitude = verticalAmplitude,
            verticalSpeed = verticalSpeed,
            phase = phase,
            formula = formula.toComponent()
        )
    }
}

data class TrailMotionFormulaDraft(
    var x: String = "",
    var y: String = "",
    var z: String = ""
) {
    fun toComponent(): TrailMotionFormulaComponent {
        return TrailMotionFormulaComponent(x = x, y = y, z = z)
    }
}

data class BeamDraft(
    var enabled: Boolean = false,
    var style: String = "mana",
    var width: Double = 0.16,
    var colorStart: String = "#FFFFFFFF",
    var colorEnd: String = "#FFFFFFFF",
    var segments: Int = 8,
    var noise: Double = 0.12,
    var texture: String = "magicrender:textures/effect/beam.png",
    var blendMode: String = "additive"
) {
    fun toComponent(): BeamComponent {
        return BeamComponent(
            enabled = enabled,
            style = style.ifBlank { "mana" },
            width = width,
            color = colorStart,
            colorStart = colorStart,
            colorEnd = colorEnd,
            segments = segments,
            noise = noise,
            texture = texture.ifBlank { "magicrender:textures/effect/beam.png" },
            blendMode = blendMode.ifBlank { "additive" }
        )
    }
}

data class MagicCircleDraft(
    var enabled: Boolean = true,
    var style: String = "arcane",
    var radius: Double = 1.6,
    var color: String = "#FFFFFFFF",
    var thickness: Double = 0.06,
    var segments: Int = 128,
    var facing: String = "face_camera",
    var rotationSpeed: Double = 1.0,
    var innerRadiusScale: Double = 0.68,
    var glyphs: Int = 12,
    var blendMode: String = "additive"
) {
    fun toComponent(): MagicCircleComponent {
        return MagicCircleComponent(
            enabled = enabled,
            style = style.ifBlank { "arcane" },
            radius = radius,
            color = color,
            thickness = thickness,
            segments = segments,
            facing = facing.ifBlank { "face_camera" },
            rotationSpeed = rotationSpeed,
            innerRadiusScale = innerRadiusScale,
            glyphs = glyphs,
            blendMode = blendMode.ifBlank { "additive" }
        )
    }
}

data class AdvancedDraft(
    var enabled: Boolean = false,
    var bloom: BloomApproximationDraft = BloomApproximationDraft(),
    var core: CoreGlowDraft = CoreGlowDraft(),
    var particleEmitters: MutableList<ParticleEmitterDraft> = mutableListOf(),
    var ribbonBundles: MutableList<RibbonBundleDraft> = mutableListOf(),
    var circleLayers: MutableList<CircleLayerDraft> = mutableListOf(),
    var radialBursts: MutableList<RadialBurstDraft> = mutableListOf()
) {
    fun toComponent(): AdvancedVisualComponent {
        return AdvancedVisualComponent(
            enabled = enabled,
            bloom = bloom.toComponent(),
            core = core.toComponent(),
            particleEmitters = particleEmitters.map { it.toComponent() },
            ribbonBundles = ribbonBundles.map { it.toComponent() },
            circleLayers = circleLayers.map { it.toComponent() },
            radialBursts = radialBursts.map { it.toComponent() }
        )
    }
}

data class BloomApproximationDraft(
    var enabled: Boolean = true,
    var layers: Int = 3,
    var scaleStep: Double = 1.8,
    var alphaFalloff: Double = 0.45
) {
    fun toComponent(): BloomApproximationComponent = BloomApproximationComponent(enabled, layers, scaleStep, alphaFalloff)
}

data class CoreGlowDraft(
    var enabled: Boolean = false,
    var color: String = "#FFFFFFFF",
    var radius: Double = 0.6,
    var pulseAmplitude: Double = 0.18,
    var pulseSpeed: Double = 0.12,
    var texture: String = "minecraft:textures/particle/flash.png",
    var blendMode: String = "additive"
) {
    fun toComponent(): CoreGlowComponent = CoreGlowComponent(enabled, color, radius, pulseAmplitude, pulseSpeed, texture, blendMode)
}

data class ParticleEmitterDraft(
    var enabled: Boolean = true,
    var shape: String = "sphere",
    var count: Int = 96,
    var colorStart: String = "#FFFFFFFF",
    var colorEnd: String = "#FFFFFFFF",
    var sizeStart: Double = 0.08,
    var sizeEnd: Double = 0.02,
    var radius: Double = 1.2,
    var height: Double = 2.0,
    var speed: Double = 0.02,
    var swirlSpeed: Double = 0.0,
    var noise: Double = 0.2,
    var texture: String = "minecraft:textures/particle/flash.png",
    var blendMode: String = "additive"
) {
    fun toComponent(): ParticleEmitterComponent {
        return ParticleEmitterComponent(enabled, shape, count, colorStart, colorEnd, sizeStart, sizeEnd, radius, height, speed, swirlSpeed, noise, texture, blendMode)
    }
}

data class RibbonBundleDraft(
    var enabled: Boolean = true,
    var count: Int = 8,
    var widthStart: Double = 0.12,
    var widthEnd: Double = 0.02,
    var colorStart: String = "#FFFFFFFF",
    var colorEnd: String = "#FFFFFFFF",
    var length: Double = 8.0,
    var samples: Int = 96,
    var phaseStep: Double = 24.0,
    var amplitude: Double = 0.8,
    var frequency: Double = 1.4,
    var twist: Double = 0.45,
    var flowSpeed: Double = 0.08,
    var texture: String = "minecraft:textures/particle/flame.png",
    var blendMode: String = "additive"
) {
    fun toComponent(): RibbonBundleComponent {
        return RibbonBundleComponent(enabled, count, widthStart, widthEnd, colorStart, colorEnd, length, samples, phaseStep, amplitude, frequency, twist, flowSpeed, texture, blendMode)
    }
}

data class CircleLayerDraft(
    var enabled: Boolean = true,
    var radius: Double = 2.0,
    var thickness: Double = 0.04,
    var color: String = "#FFFFFFFF",
    var segments: Int = 128,
    var rotationSpeed: Double = 1.0,
    var glyphs: Int = 0,
    var glyphMode: String = "ticks",
    var facing: String = "face_camera",
    var blendMode: String = "additive"
) {
    fun toComponent(): CircleLayerComponent {
        return CircleLayerComponent(enabled, radius, thickness, color, segments, rotationSpeed, glyphs, glyphMode, facing, blendMode)
    }
}

data class RadialBurstDraft(
    var enabled: Boolean = true,
    var rays: Int = 16,
    var length: Double = 2.8,
    var widthStart: Double = 0.08,
    var widthEnd: Double = 0.0,
    var colorStart: String = "#FFFFFFFF",
    var colorEnd: String = "#FFFFFF00",
    var rotationSpeed: Double = 0.0,
    var randomJitter: Double = 0.15,
    var texture: String = "minecraft:textures/particle/flash.png",
    var blendMode: String = "additive"
) {
    fun toComponent(): RadialBurstComponent {
        return RadialBurstComponent(enabled, rays, length, widthStart, widthEnd, colorStart, colorEnd, rotationSpeed, randomJitter, texture, blendMode)
    }
}
