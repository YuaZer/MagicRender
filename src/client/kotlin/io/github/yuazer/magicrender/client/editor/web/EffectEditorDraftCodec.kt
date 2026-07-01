package io.github.yuazer.magicrender.client.editor.web

import com.google.gson.JsonObject
import io.github.yuazer.magicrender.client.editor.AdvancedDraft
import io.github.yuazer.magicrender.client.editor.BeamDraft
import io.github.yuazer.magicrender.client.editor.BloomApproximationDraft
import io.github.yuazer.magicrender.client.editor.CircleLayerDraft
import io.github.yuazer.magicrender.client.editor.ComponentDrafts
import io.github.yuazer.magicrender.client.editor.CoreGlowDraft
import io.github.yuazer.magicrender.client.editor.EffectEditorDraft
import io.github.yuazer.magicrender.client.editor.GlowPostDraft
import io.github.yuazer.magicrender.client.editor.MagicCircleDraft
import io.github.yuazer.magicrender.client.editor.ParticleEmitterDraft
import io.github.yuazer.magicrender.client.editor.PreviewDraft
import io.github.yuazer.magicrender.client.editor.RadialBurstDraft
import io.github.yuazer.magicrender.client.editor.RibbonBundleDraft
import io.github.yuazer.magicrender.client.editor.TrailDraft
import io.github.yuazer.magicrender.client.editor.TrailMotionDraft
import io.github.yuazer.magicrender.client.editor.TrailMotionFormulaDraft
import io.github.yuazer.magicrender.client.editor.VisibilityDraft
import io.github.yuazer.magicrender.config.boolean
import io.github.yuazer.magicrender.config.obj
import io.github.yuazer.magicrender.config.objectList
import io.github.yuazer.magicrender.config.string

object EffectEditorDraftCodec {
    fun toEditorJson(draft: EffectEditorDraft): JsonObject {
        val json = draft.toJsonObject()
        val preview = JsonObject()
        preview.addProperty("targetMode", draft.preview.targetMode)
        preview.addProperty("fixedDistance", draft.preview.fixedDistance)
        preview.addProperty("sourceHeightOffset", draft.preview.sourceHeightOffset)
        preview.addProperty("targetHeightOffset", draft.preview.targetHeightOffset)
        preview.addProperty("fallbackToFixedDistance", draft.preview.fallbackToFixedDistance)
        json.add("preview", preview)
        return json
    }

    fun fromJson(json: JsonObject): EffectEditorDraft {
        val default = EffectEditorDraft.entityArcaneStream()
        val visibilityJson = json.obj("visibility")
        val componentsJson = json.obj("components")
        return EffectEditorDraft(
            version = json.intLoose("version", default.version),
            id = json.string("id", default.id),
            enabled = json.boolean("enabled", default.enabled),
            group = json.string("group", default.group),
            durationTicks = json.intLoose("durationTicks", default.durationTicks),
            importance = json.string("importance", default.importance),
            visibility = VisibilityDraft(
                drawDistance = visibilityJson?.intLoose("drawDistance", default.visibility.drawDistance) ?: default.visibility.drawDistance,
                hideWhenShadersConflict = visibilityJson?.boolean("hideWhenShadersConflict", default.visibility.hideWhenShadersConflict) ?: default.visibility.hideWhenShadersConflict
            ),
            components = ComponentDrafts(
                trail = trail(componentsJson?.obj("trail"), default.components.trail),
                beam = beam(componentsJson?.obj("beam"), default.components.beam),
                magicCircle = circle(componentsJson?.obj("magicCircle"), default.components.magicCircle),
                advanced = advanced(componentsJson?.obj("advanced"), default.components.advanced)
            ),
            preview = preview(json.obj("preview"), default.preview)
        )
    }

    private fun trail(json: JsonObject?, default: TrailDraft): TrailDraft {
        if (json == null) return default
        val width = json.obj("width")
        val color = json.obj("color")
        return TrailDraft(
            enabled = json.boolean("enabled", default.enabled),
            style = json.string("style", default.style),
            widthStart = width?.doubleLoose("start", default.widthStart) ?: json.doubleLoose("widthStart", default.widthStart),
            widthEnd = width?.doubleLoose("end", default.widthEnd) ?: json.doubleLoose("widthEnd", default.widthEnd),
            colorStart = color?.string("start", default.colorStart) ?: json.string("colorStart", default.colorStart),
            colorEnd = color?.string("end", default.colorEnd) ?: json.string("colorEnd", default.colorEnd),
            lifetimeTicks = json.intLoose("lifetimeTicks", default.lifetimeTicks),
            maxPoints = json.intLoose("maxPoints", default.maxPoints),
            minSampleDistance = json.doubleLoose("minSampleDistance", default.minSampleDistance),
            maxSegmentLength = json.doubleLoose("maxSegmentLength", default.maxSegmentLength),
            maxInsertedPointsPerTick = json.intLoose("maxInsertedPointsPerTick", default.maxInsertedPointsPerTick),
            sampleEveryTick = json.boolean("sampleEveryTick", default.sampleEveryTick),
            renderMode = json.string("renderMode", default.renderMode),
            texture = json.string("texture", default.texture),
            blendMode = json.string("blendMode", default.blendMode),
            motion = motion(json.obj("motion"), default.motion)
        )
    }

    private fun motion(json: JsonObject?, default: TrailMotionDraft): TrailMotionDraft {
        if (json == null) return default
        return TrailMotionDraft(
            mode = json.string("mode", default.mode),
            radius = json.doubleLoose("radius", default.radius),
            angularSpeed = json.doubleLoose("angularSpeed", default.angularSpeed),
            verticalAmplitude = json.doubleLoose("verticalAmplitude", default.verticalAmplitude),
            verticalSpeed = json.doubleLoose("verticalSpeed", default.verticalSpeed),
            phase = json.doubleLoose("phase", default.phase),
            formula = formula(json.obj("formula"), default.formula)
        )
    }

    private fun formula(json: JsonObject?, default: TrailMotionFormulaDraft): TrailMotionFormulaDraft {
        if (json == null) return default
        return TrailMotionFormulaDraft(
            x = json.string("x", default.x),
            y = json.string("y", default.y),
            z = json.string("z", default.z)
        )
    }

    private fun beam(json: JsonObject?, default: BeamDraft): BeamDraft {
        if (json == null) return default
        val color = json.obj("color")
        return BeamDraft(
            enabled = json.boolean("enabled", default.enabled),
            style = json.string("style", default.style),
            width = json.doubleLoose("width", default.width),
            colorStart = color?.string("start", default.colorStart) ?: json.string("colorStart", default.colorStart),
            colorEnd = color?.string("end", default.colorEnd) ?: json.string("colorEnd", default.colorEnd),
            segments = json.intLoose("segments", default.segments),
            noise = json.doubleLoose("noise", default.noise),
            texture = json.string("texture", default.texture),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun circle(json: JsonObject?, default: MagicCircleDraft): MagicCircleDraft {
        if (json == null) return default
        return MagicCircleDraft(
            enabled = json.boolean("enabled", default.enabled),
            style = json.string("style", default.style),
            radius = json.doubleLoose("radius", default.radius),
            color = json.string("color", default.color),
            thickness = json.doubleLoose("thickness", default.thickness),
            segments = json.intLoose("segments", default.segments),
            facing = json.string("facing", default.facing),
            rotationSpeed = json.doubleLoose("rotationSpeed", default.rotationSpeed),
            innerRadiusScale = json.doubleLoose("innerRadiusScale", default.innerRadiusScale),
            glyphs = json.intLoose("glyphs", default.glyphs),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun preview(json: JsonObject?, default: PreviewDraft): PreviewDraft {
        if (json == null) return default
        return PreviewDraft(
            targetMode = json.string("targetMode", default.targetMode),
            fixedDistance = json.doubleLoose("fixedDistance", default.fixedDistance),
            sourceHeightOffset = json.doubleLoose("sourceHeightOffset", default.sourceHeightOffset),
            targetHeightOffset = json.doubleLoose("targetHeightOffset", default.targetHeightOffset),
            fallbackToFixedDistance = json.boolean("fallbackToFixedDistance", default.fallbackToFixedDistance)
        )
    }

    private fun advanced(json: JsonObject?, default: AdvancedDraft): AdvancedDraft {
        if (json == null) return default
        return AdvancedDraft(
            enabled = json.boolean("enabled", default.enabled),
            bloom = bloom(json.obj("bloom"), default.bloom),
            glow = glow(json.obj("glow"), default.glow),
            core = core(json.obj("core"), default.core),
            particleEmitters = json.objectList("particleEmitters").map { particleEmitter(it, ParticleEmitterDraft()) }.toMutableList(),
            ribbonBundles = json.objectList("ribbonBundles").map { ribbonBundle(it, RibbonBundleDraft()) }.toMutableList(),
            circleLayers = json.objectList("circleLayers").map { circleLayer(it, CircleLayerDraft()) }.toMutableList(),
            radialBursts = json.objectList("radialBursts").map { radialBurst(it, RadialBurstDraft()) }.toMutableList()
        )
    }

    private fun glow(json: JsonObject?, default: GlowPostDraft): GlowPostDraft {
        if (json == null) return default
        return GlowPostDraft(
            enabled = json.boolean("enabled", default.enabled),
            intensity = json.doubleLoose("intensity", default.intensity),
            radius = json.doubleLoose("radius", default.radius),
            iterations = json.intLoose("iterations", default.iterations),
            downsample = json.intLoose("downsample", default.downsample),
            threshold = json.doubleLoose("threshold", default.threshold)
        )
    }

    private fun bloom(json: JsonObject?, default: BloomApproximationDraft): BloomApproximationDraft {
        if (json == null) return default
        return BloomApproximationDraft(
            enabled = json.boolean("enabled", default.enabled),
            layers = json.intLoose("layers", default.layers),
            scaleStep = json.doubleLoose("scaleStep", default.scaleStep),
            alphaFalloff = json.doubleLoose("alphaFalloff", default.alphaFalloff)
        )
    }

    private fun core(json: JsonObject?, default: CoreGlowDraft): CoreGlowDraft {
        if (json == null) return default
        return CoreGlowDraft(
            enabled = json.boolean("enabled", default.enabled),
            color = json.string("color", default.color),
            radius = json.doubleLoose("radius", default.radius),
            pulseAmplitude = json.doubleLoose("pulseAmplitude", default.pulseAmplitude),
            pulseSpeed = json.doubleLoose("pulseSpeed", default.pulseSpeed),
            texture = json.string("texture", default.texture),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun particleEmitter(json: JsonObject, default: ParticleEmitterDraft): ParticleEmitterDraft {
        val color = json.obj("color")
        val size = json.obj("size")
        return ParticleEmitterDraft(
            enabled = json.boolean("enabled", default.enabled),
            shape = json.string("shape", default.shape),
            count = json.intLoose("count", default.count),
            colorStart = color?.string("start", default.colorStart) ?: json.string("colorStart", default.colorStart),
            colorEnd = color?.string("end", default.colorEnd) ?: json.string("colorEnd", default.colorEnd),
            sizeStart = size?.doubleLoose("start", default.sizeStart) ?: json.doubleLoose("sizeStart", default.sizeStart),
            sizeEnd = size?.doubleLoose("end", default.sizeEnd) ?: json.doubleLoose("sizeEnd", default.sizeEnd),
            radius = json.doubleLoose("radius", default.radius),
            height = json.doubleLoose("height", default.height),
            speed = json.doubleLoose("speed", default.speed),
            swirlSpeed = json.doubleLoose("swirlSpeed", default.swirlSpeed),
            noise = json.doubleLoose("noise", default.noise),
            texture = json.string("texture", default.texture),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun ribbonBundle(json: JsonObject, default: RibbonBundleDraft): RibbonBundleDraft {
        val color = json.obj("color")
        val width = json.obj("width")
        return RibbonBundleDraft(
            enabled = json.boolean("enabled", default.enabled),
            count = json.intLoose("count", default.count),
            widthStart = width?.doubleLoose("start", default.widthStart) ?: json.doubleLoose("widthStart", default.widthStart),
            widthEnd = width?.doubleLoose("end", default.widthEnd) ?: json.doubleLoose("widthEnd", default.widthEnd),
            colorStart = color?.string("start", default.colorStart) ?: json.string("colorStart", default.colorStart),
            colorEnd = color?.string("end", default.colorEnd) ?: json.string("colorEnd", default.colorEnd),
            length = json.doubleLoose("length", default.length),
            samples = json.intLoose("samples", default.samples),
            phaseStep = json.doubleLoose("phaseStep", default.phaseStep),
            amplitude = json.doubleLoose("amplitude", default.amplitude),
            frequency = json.doubleLoose("frequency", default.frequency),
            twist = json.doubleLoose("twist", default.twist),
            flowSpeed = json.doubleLoose("flowSpeed", default.flowSpeed),
            texture = json.string("texture", default.texture),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun circleLayer(json: JsonObject, default: CircleLayerDraft): CircleLayerDraft {
        return CircleLayerDraft(
            enabled = json.boolean("enabled", default.enabled),
            radius = json.doubleLoose("radius", default.radius),
            thickness = json.doubleLoose("thickness", default.thickness),
            color = json.string("color", default.color),
            segments = json.intLoose("segments", default.segments),
            rotationSpeed = json.doubleLoose("rotationSpeed", default.rotationSpeed),
            glyphs = json.intLoose("glyphs", default.glyphs),
            glyphMode = json.string("glyphMode", default.glyphMode),
            facing = json.string("facing", default.facing),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun radialBurst(json: JsonObject, default: RadialBurstDraft): RadialBurstDraft {
        val color = json.obj("color")
        val width = json.obj("width")
        return RadialBurstDraft(
            enabled = json.boolean("enabled", default.enabled),
            rays = json.intLoose("rays", default.rays),
            length = json.doubleLoose("length", default.length),
            widthStart = width?.doubleLoose("start", default.widthStart) ?: json.doubleLoose("widthStart", default.widthStart),
            widthEnd = width?.doubleLoose("end", default.widthEnd) ?: json.doubleLoose("widthEnd", default.widthEnd),
            colorStart = color?.string("start", default.colorStart) ?: json.string("colorStart", default.colorStart),
            colorEnd = color?.string("end", default.colorEnd) ?: json.string("colorEnd", default.colorEnd),
            rotationSpeed = json.doubleLoose("rotationSpeed", default.rotationSpeed),
            randomJitter = json.doubleLoose("randomJitter", default.randomJitter),
            texture = json.string("texture", default.texture),
            blendMode = json.string("blendMode", default.blendMode)
        )
    }

    private fun JsonObject.intLoose(name: String, default: Int): Int {
        return try {
            get(name)?.asInt ?: default
        } catch (_: Exception) {
            default
        }
    }

    private fun JsonObject.doubleLoose(name: String, default: Double): Double {
        return try {
            get(name)?.asDouble ?: default
        } catch (_: Exception) {
            default
        }
    }
}
