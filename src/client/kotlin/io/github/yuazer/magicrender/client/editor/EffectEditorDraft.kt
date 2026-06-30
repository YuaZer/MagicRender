package io.github.yuazer.magicrender.client.editor

import com.google.gson.JsonObject
import io.github.yuazer.magicrender.config.BeamComponent
import io.github.yuazer.magicrender.config.EffectComponents
import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.EffectVisibility
import io.github.yuazer.magicrender.config.MagicCircleComponent
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
                trail = components.trail.toComponent()
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
    var magicCircle: MagicCircleDraft = MagicCircleDraft()
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
