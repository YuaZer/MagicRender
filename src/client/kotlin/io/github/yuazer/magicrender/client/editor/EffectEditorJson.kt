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
}
