package io.github.yuazer.magicrender.client.editor

import io.github.yuazer.magicrender.config.isColorString
import io.github.yuazer.magicrender.config.isValidIdentifier
import io.github.yuazer.magicrender.i18n.MagicRenderI18n.tr
import net.minecraft.network.chat.Component

data class EffectEditorValidationResult(
    val errors: List<Component>,
    val warnings: List<Component>
) {
    val canExport: Boolean get() = errors.isEmpty()
    val summary: String
        get() = when {
            errors.isNotEmpty() -> "errors=${errors.size}, warnings=${warnings.size}"
            warnings.isNotEmpty() -> "warnings=${warnings.size}"
            else -> "ok"
        }
}

object EffectEditorValidation {
    fun validate(draft: EffectEditorDraft): EffectEditorValidationResult {
        val errors = mutableListOf<Component>()
        val warnings = mutableListOf<Component>()

        if (!isValidIdentifier(draft.id)) {
            errors += tr("magicrender.editor.validation.id")
        }
        if (draft.group.isBlank()) warnings += tr("magicrender.editor.validation.group_blank")
        range(draft.durationTicks, 1, 20 * 60, "durationTicks", errors)
        range(draft.visibility.drawDistance, 0, 256, "visibility.drawDistance", errors)

        validateTrail(draft.components.trail, errors, warnings)
        validateBeam(draft.components.beam, errors)
        validateCircle(draft.components.magicCircle, errors)
        validateAdvanced(draft.components.advanced, errors)
        validatePreview(draft.preview, errors)

        if (!draft.components.trail.enabled && !draft.components.beam.enabled && !draft.components.magicCircle.enabled && !draft.components.advanced.enabled) {
            warnings += tr("magicrender.editor.validation.no_world_component")
        }

        return EffectEditorValidationResult(errors, warnings)
    }

    private fun validateTrail(trail: TrailDraft, errors: MutableList<Component>, warnings: MutableList<Component>) {
        range(trail.widthStart, 0.01, 8.0, "trail.width.start", errors)
        range(trail.widthEnd, 0.0, 8.0, "trail.width.end", errors)
        color(trail.colorStart, "trail.color.start", errors)
        color(trail.colorEnd, "trail.color.end", errors)
        range(trail.lifetimeTicks, 1, 200, "trail.lifetimeTicks", errors)
        range(trail.maxPoints, 2, 128, "trail.maxPoints", errors)
        range(trail.minSampleDistance, 0.01, 4.0, "trail.minSampleDistance", errors)
        range(trail.maxSegmentLength, 0.05, 8.0, "trail.maxSegmentLength", errors)
        range(trail.maxInsertedPointsPerTick, 0, 8, "trail.maxInsertedPointsPerTick", errors)
        identifier(trail.texture, "trail.texture", errors)
        if (trail.maxPoints > 96) warnings += tr("magicrender.editor.validation.trail_max_points_expensive")
        if (trail.motion.mode !in setOf("follow", "orbit", "helix", "spiral", "formula", "js", "javascript")) {
            errors += tr("magicrender.editor.validation.motion_mode")
        }
        range(trail.motion.radius, 0.0, 16.0, "trail.motion.radius", errors)
        range(trail.motion.angularSpeed, -72.0, 72.0, "trail.motion.angularSpeed", errors)
        range(trail.motion.verticalAmplitude, 0.0, 16.0, "trail.motion.verticalAmplitude", errors)
        range(trail.motion.verticalSpeed, -72.0, 72.0, "trail.motion.verticalSpeed", errors)
        formula(trail.motion.formula.x, "trail.motion.formula.x", errors)
        formula(trail.motion.formula.y, "trail.motion.formula.y", errors)
        formula(trail.motion.formula.z, "trail.motion.formula.z", errors)
    }

    private fun validateBeam(beam: BeamDraft, errors: MutableList<Component>) {
        range(beam.width, 0.01, 4.0, "beam.width", errors)
        color(beam.colorStart, "beam.color.start", errors)
        color(beam.colorEnd, "beam.color.end", errors)
        range(beam.segments, 1, 64, "beam.segments", errors)
        range(beam.noise, 0.0, 4.0, "beam.noise", errors)
        identifier(beam.texture, "beam.texture", errors)
    }

    private fun validateCircle(circle: MagicCircleDraft, errors: MutableList<Component>) {
        range(circle.radius, 0.1, 32.0, "magicCircle.radius", errors)
        color(circle.color, "magicCircle.color", errors)
        range(circle.thickness, 0.01, 2.0, "magicCircle.thickness", errors)
        range(circle.segments, 16, 256, "magicCircle.segments", errors)
        range(circle.rotationSpeed, -16.0, 16.0, "magicCircle.rotationSpeed", errors)
        range(circle.innerRadiusScale, 0.1, 0.95, "magicCircle.innerRadiusScale", errors)
        range(circle.glyphs, 0, 64, "magicCircle.glyphs", errors)
    }

    private fun validatePreview(preview: PreviewDraft, errors: MutableList<Component>) {
        if (preview.targetMode !in setOf("fixed_distance", "crosshair_entity", "look_point")) {
            errors += tr("magicrender.editor.validation.preview_target_mode")
        }
        range(preview.fixedDistance, 0.5, 64.0, "preview.fixedDistance", errors)
        range(preview.sourceHeightOffset, -2.0, 3.0, "preview.sourceHeightOffset", errors)
        range(preview.targetHeightOffset, -2.0, 3.0, "preview.targetHeightOffset", errors)
    }

    private fun validateAdvanced(advanced: AdvancedDraft, errors: MutableList<Component>) {
        range(advanced.bloom.layers, 0, 8, "advanced.bloom.layers", errors)
        range(advanced.bloom.scaleStep, 1.0, 6.0, "advanced.bloom.scaleStep", errors)
        range(advanced.bloom.alphaFalloff, 0.05, 1.0, "advanced.bloom.alphaFalloff", errors)
        color(advanced.core.color, "advanced.core.color", errors)
        range(advanced.core.radius, 0.01, 16.0, "advanced.core.radius", errors)
        identifier(advanced.core.texture, "advanced.core.texture", errors)
        advanced.particleEmitters.forEachIndexed { index, emitter ->
            color(emitter.colorStart, "advanced.particleEmitters[$index].color.start", errors)
            color(emitter.colorEnd, "advanced.particleEmitters[$index].color.end", errors)
            range(emitter.count, 0, 4096, "advanced.particleEmitters[$index].count", errors)
            range(emitter.sizeStart, 0.005, 4.0, "advanced.particleEmitters[$index].size.start", errors)
            range(emitter.sizeEnd, 0.0, 4.0, "advanced.particleEmitters[$index].size.end", errors)
            identifier(emitter.texture, "advanced.particleEmitters[$index].texture", errors)
        }
        advanced.ribbonBundles.forEachIndexed { index, bundle ->
            color(bundle.colorStart, "advanced.ribbonBundles[$index].color.start", errors)
            color(bundle.colorEnd, "advanced.ribbonBundles[$index].color.end", errors)
            range(bundle.count, 0, 64, "advanced.ribbonBundles[$index].count", errors)
            range(bundle.samples, 2, 256, "advanced.ribbonBundles[$index].samples", errors)
            identifier(bundle.texture, "advanced.ribbonBundles[$index].texture", errors)
        }
        advanced.circleLayers.forEachIndexed { index, layer ->
            color(layer.color, "advanced.circleLayers[$index].color", errors)
            range(layer.radius, 0.01, 64.0, "advanced.circleLayers[$index].radius", errors)
            range(layer.segments, 8, 512, "advanced.circleLayers[$index].segments", errors)
        }
        advanced.radialBursts.forEachIndexed { index, burst ->
            color(burst.colorStart, "advanced.radialBursts[$index].color.start", errors)
            color(burst.colorEnd, "advanced.radialBursts[$index].color.end", errors)
            range(burst.rays, 0, 256, "advanced.radialBursts[$index].rays", errors)
            identifier(burst.texture, "advanced.radialBursts[$index].texture", errors)
        }
    }

    private fun identifier(value: String, path: String, errors: MutableList<Component>) {
        if (!isValidIdentifier(value)) errors += tr("magicrender.editor.validation.resource_location", path)
    }

    private fun color(value: String, path: String, errors: MutableList<Component>) {
        if (!isColorString(value)) errors += tr("magicrender.editor.validation.color", path)
    }

    private fun range(value: Int, min: Int, max: Int, path: String, errors: MutableList<Component>) {
        if (value < min || value > max) errors += tr("magicrender.editor.validation.range", path, min, max)
    }

    private fun range(value: Double, min: Double, max: Double, path: String, errors: MutableList<Component>) {
        if (value < min || value > max) errors += tr("magicrender.editor.validation.range", path, min, max)
    }

    private fun formula(value: String, path: String, errors: MutableList<Component>) {
        if (value.length > 240) errors += tr("magicrender.editor.validation.range", "$path.length", 0, 240)
    }
}
