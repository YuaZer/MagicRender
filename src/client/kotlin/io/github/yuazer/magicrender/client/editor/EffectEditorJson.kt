package io.github.yuazer.magicrender.client.editor

import com.google.gson.JsonObject
import io.github.yuazer.magicrender.config.EffectDefinition

object EffectEditorJson {
    fun toJsonObject(effect: EffectDefinition): JsonObject {
        val root = JsonObject()
        root.addProperty("version", effect.version)
        root.addProperty("id", effect.id)
        root.addProperty("enabled", effect.enabled)
        root.addProperty("group", effect.group)
        root.addProperty("durationTicks", effect.durationTicks)
        root.addProperty("importance", effect.importance)

        val visibility = JsonObject()
        visibility.addProperty("drawDistance", effect.visibility.drawDistance)
        visibility.addProperty("hideWhenShadersConflict", effect.visibility.hideWhenShadersConflict)
        root.add("visibility", visibility)

        val components = JsonObject()
        components.add("trail", trail(effect))
        components.add("magicCircle", magicCircle(effect))
        components.add("beam", beam(effect))
        components.add("advanced", advanced(effect))
        root.add("components", components)
        return root
    }

    private fun trail(effect: EffectDefinition): JsonObject {
        val trail = effect.components.trail
        val json = JsonObject()
        json.addProperty("enabled", trail.enabled)
        json.addProperty("style", trail.style)
        val width = JsonObject()
        width.addProperty("start", trail.widthStart)
        width.addProperty("end", trail.widthEnd)
        json.add("width", width)
        val color = JsonObject()
        color.addProperty("start", trail.colorStart)
        color.addProperty("end", trail.colorEnd)
        json.add("color", color)
        json.addProperty("lifetimeTicks", trail.lifetimeTicks)
        json.addProperty("maxPoints", trail.maxPoints)
        json.addProperty("minSampleDistance", trail.minSampleDistance)
        json.addProperty("maxSegmentLength", trail.maxSegmentLength)
        json.addProperty("maxInsertedPointsPerTick", trail.maxInsertedPointsPerTick)
        json.addProperty("sampleEveryTick", trail.sampleEveryTick)
        json.addProperty("renderMode", trail.renderMode)
        json.addProperty("texture", trail.texture)
        json.addProperty("blendMode", trail.blendMode)

        val motion = JsonObject()
        motion.addProperty("mode", trail.motion.mode)
        motion.addProperty("radius", trail.motion.radius)
        motion.addProperty("angularSpeed", trail.motion.angularSpeed)
        motion.addProperty("verticalAmplitude", trail.motion.verticalAmplitude)
        motion.addProperty("verticalSpeed", trail.motion.verticalSpeed)
        motion.addProperty("phase", trail.motion.phase)
        val formula = JsonObject()
        formula.addProperty("x", trail.motion.formula.x)
        formula.addProperty("y", trail.motion.formula.y)
        formula.addProperty("z", trail.motion.formula.z)
        motion.add("formula", formula)
        json.add("motion", motion)
        return json
    }

    private fun magicCircle(effect: EffectDefinition): JsonObject {
        val circle = effect.components.magicCircle
        val json = JsonObject()
        json.addProperty("enabled", circle.enabled)
        json.addProperty("style", circle.style)
        json.addProperty("radius", circle.radius)
        json.addProperty("color", circle.color)
        json.addProperty("thickness", circle.thickness)
        json.addProperty("segments", circle.segments)
        json.addProperty("facing", circle.facing)
        json.addProperty("rotationSpeed", circle.rotationSpeed)
        json.addProperty("innerRadiusScale", circle.innerRadiusScale)
        json.addProperty("glyphs", circle.glyphs)
        json.addProperty("blendMode", circle.blendMode)
        return json
    }

    private fun beam(effect: EffectDefinition): JsonObject {
        val beam = effect.components.beam
        val json = JsonObject()
        json.addProperty("enabled", beam.enabled)
        json.addProperty("style", beam.style)
        json.addProperty("width", beam.width)
        val color = JsonObject()
        color.addProperty("start", beam.colorStart)
        color.addProperty("end", beam.colorEnd)
        json.add("color", color)
        json.addProperty("segments", beam.segments)
        json.addProperty("noise", beam.noise)
        json.addProperty("texture", beam.texture)
        json.addProperty("blendMode", beam.blendMode)
        return json
    }

    private fun advanced(effect: EffectDefinition): JsonObject {
        val advanced = effect.components.advanced
        val json = JsonObject()
        json.addProperty("enabled", advanced.enabled)
        val bloom = JsonObject()
        bloom.addProperty("enabled", advanced.bloom.enabled)
        bloom.addProperty("layers", advanced.bloom.layers)
        bloom.addProperty("scaleStep", advanced.bloom.scaleStep)
        bloom.addProperty("alphaFalloff", advanced.bloom.alphaFalloff)
        json.add("bloom", bloom)

        val glow = JsonObject()
        glow.addProperty("enabled", advanced.glow.enabled)
        glow.addProperty("intensity", advanced.glow.intensity)
        glow.addProperty("radius", advanced.glow.radius)
        glow.addProperty("iterations", advanced.glow.iterations)
        glow.addProperty("downsample", advanced.glow.downsample)
        glow.addProperty("threshold", advanced.glow.threshold)
        json.add("glow", glow)

        val core = JsonObject()
        core.addProperty("enabled", advanced.core.enabled)
        core.addProperty("color", advanced.core.color)
        core.addProperty("radius", advanced.core.radius)
        core.addProperty("pulseAmplitude", advanced.core.pulseAmplitude)
        core.addProperty("pulseSpeed", advanced.core.pulseSpeed)
        core.addProperty("texture", advanced.core.texture)
        core.addProperty("blendMode", advanced.core.blendMode)
        json.add("core", core)

        val emitters = com.google.gson.JsonArray()
        for (emitter in advanced.particleEmitters) {
            val item = JsonObject()
            item.addProperty("enabled", emitter.enabled)
            item.addProperty("shape", emitter.shape)
            item.addProperty("count", emitter.count)
            val color = JsonObject()
            color.addProperty("start", emitter.colorStart)
            color.addProperty("end", emitter.colorEnd)
            item.add("color", color)
            val size = JsonObject()
            size.addProperty("start", emitter.sizeStart)
            size.addProperty("end", emitter.sizeEnd)
            item.add("size", size)
            item.addProperty("radius", emitter.radius)
            item.addProperty("height", emitter.height)
            item.addProperty("speed", emitter.speed)
            item.addProperty("swirlSpeed", emitter.swirlSpeed)
            item.addProperty("noise", emitter.noise)
            item.addProperty("texture", emitter.texture)
            item.addProperty("blendMode", emitter.blendMode)
            emitters.add(item)
        }
        json.add("particleEmitters", emitters)

        val bundles = com.google.gson.JsonArray()
        for (bundle in advanced.ribbonBundles) {
            val item = JsonObject()
            item.addProperty("enabled", bundle.enabled)
            item.addProperty("count", bundle.count)
            val width = JsonObject()
            width.addProperty("start", bundle.widthStart)
            width.addProperty("end", bundle.widthEnd)
            item.add("width", width)
            val color = JsonObject()
            color.addProperty("start", bundle.colorStart)
            color.addProperty("end", bundle.colorEnd)
            item.add("color", color)
            item.addProperty("length", bundle.length)
            item.addProperty("samples", bundle.samples)
            item.addProperty("phaseStep", bundle.phaseStep)
            item.addProperty("amplitude", bundle.amplitude)
            item.addProperty("frequency", bundle.frequency)
            item.addProperty("twist", bundle.twist)
            item.addProperty("flowSpeed", bundle.flowSpeed)
            item.addProperty("texture", bundle.texture)
            item.addProperty("blendMode", bundle.blendMode)
            bundles.add(item)
        }
        json.add("ribbonBundles", bundles)

        val layers = com.google.gson.JsonArray()
        for (layer in advanced.circleLayers) {
            val item = JsonObject()
            item.addProperty("enabled", layer.enabled)
            item.addProperty("radius", layer.radius)
            item.addProperty("thickness", layer.thickness)
            item.addProperty("color", layer.color)
            item.addProperty("segments", layer.segments)
            item.addProperty("rotationSpeed", layer.rotationSpeed)
            item.addProperty("glyphs", layer.glyphs)
            item.addProperty("glyphMode", layer.glyphMode)
            item.addProperty("facing", layer.facing)
            item.addProperty("blendMode", layer.blendMode)
            layers.add(item)
        }
        json.add("circleLayers", layers)

        val bursts = com.google.gson.JsonArray()
        for (burst in advanced.radialBursts) {
            val item = JsonObject()
            item.addProperty("enabled", burst.enabled)
            item.addProperty("rays", burst.rays)
            item.addProperty("length", burst.length)
            val width = JsonObject()
            width.addProperty("start", burst.widthStart)
            width.addProperty("end", burst.widthEnd)
            item.add("width", width)
            val color = JsonObject()
            color.addProperty("start", burst.colorStart)
            color.addProperty("end", burst.colorEnd)
            item.add("color", color)
            item.addProperty("rotationSpeed", burst.rotationSpeed)
            item.addProperty("randomJitter", burst.randomJitter)
            item.addProperty("texture", burst.texture)
            item.addProperty("blendMode", burst.blendMode)
            bursts.add(item)
        }
        json.add("radialBursts", bursts)
        return json
    }
}
