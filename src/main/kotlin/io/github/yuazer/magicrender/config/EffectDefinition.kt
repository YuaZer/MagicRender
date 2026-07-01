package io.github.yuazer.magicrender.config

import com.google.gson.JsonObject

data class EffectDefinition(
    val version: Int,
    val id: String,
    val enabled: Boolean,
    val group: String,
    val durationTicks: Int,
    val importance: String,
    val visibility: EffectVisibility,
    val components: EffectComponents
) {
    companion object {
        fun parse(json: JsonObject, fileName: String, common: CommonConfig, groups: EffectGroupConfig, result: ConfigLoadAccumulator): EffectDefinition? {
            val path = "effects/$fileName"
            json.warnUnknownFields(setOf("version", "id", "enabled", "group", "durationTicks", "importance", "visibility", "components"), path, result)
            val id = json.string("id", "")
            if (!isValidIdentifier(id)) {
                result.error(path, "`id` must use namespace:path format with lowercase characters.")
                return null
            }

            val requestedGroup = json.string("group", "default")
            val group = if (groups.groups.containsKey(requestedGroup)) {
                requestedGroup
            } else {
                result.warning(path, "Unknown group `$requestedGroup`. Using `default`.")
                "default"
            }

            return EffectDefinition(
                version = json.int("version", 1, 1, 1, path, result),
                id = id,
                enabled = json.boolean("enabled", true),
                group = group,
                durationTicks = json.int("durationTicks", 40, 1, 20 * 60, path, result),
                importance = json.string("importance", "normal"),
                visibility = EffectVisibility.parse(json.obj("visibility"), common, "$path.visibility", result),
                components = EffectComponents.parse(json.obj("components"), common, "$path.components", result)
            )
        }
    }
}

data class EffectVisibility(
    val drawDistance: Int = 64,
    val hideWhenShadersConflict: Boolean = false
) {
    companion object {
        fun parse(json: JsonObject?, common: CommonConfig, path: String, result: ConfigLoadAccumulator): EffectVisibility {
            if (json == null) return EffectVisibility(drawDistance = common.limits.defaultDrawDistance)
            return EffectVisibility(
                drawDistance = json.int("drawDistance", common.limits.defaultDrawDistance, 0, common.limits.importantDrawDistance, path, result),
                hideWhenShadersConflict = json.boolean("hideWhenShadersConflict", false)
            )
        }
    }
}

data class EffectComponents(
    val particles: ParticleComponent = ParticleComponent(enabled = false),
    val magicCircle: MagicCircleComponent = MagicCircleComponent(enabled = false),
    val aura: AuraComponent = AuraComponent(enabled = false),
    val beam: BeamComponent = BeamComponent(enabled = false),
    val trail: TrailComponent = TrailComponent(enabled = false),
    val shockwave: ShockwaveComponent = ShockwaveComponent(enabled = false),
    val advanced: AdvancedVisualComponent = AdvancedVisualComponent(enabled = false),
    val sound: SoundComponent = SoundComponent(enabled = false)
) {
    companion object {
        fun parse(json: JsonObject?, common: CommonConfig, path: String, result: ConfigLoadAccumulator): EffectComponents {
            if (json == null) return EffectComponents()
            return EffectComponents(
                particles = ParticleComponent.parse(json.obj("particles"), common, "$path.particles", result),
                magicCircle = MagicCircleComponent.parse(json.obj("magicCircle"), "$path.magicCircle", result),
                aura = AuraComponent.parse(json.obj("aura"), "$path.aura", result),
                beam = BeamComponent.parse(json.obj("beam"), "$path.beam", result),
                trail = TrailComponent.parse(json.obj("trail"), "$path.trail", result),
                shockwave = ShockwaveComponent.parse(json.obj("shockwave"), "$path.shockwave", result),
                advanced = AdvancedVisualComponent.parse(json.obj("advanced"), common, "$path.advanced", result),
                sound = SoundComponent.parse(json.obj("sound"), "$path.sound", result)
            )
        }
    }
}

data class ParticleComponent(
    val enabled: Boolean,
    val style: String = "spark",
    val amount: Int = 16,
    val color: String = "#FFFFFFFF",
    val size: Double = 0.25
) {
    companion object {
        fun parse(json: JsonObject?, common: CommonConfig, path: String, result: ConfigLoadAccumulator): ParticleComponent {
            if (json == null) return ParticleComponent(enabled = false)
            val color = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            return ParticleComponent(
                enabled = json.boolean("enabled", true),
                style = json.string("style", "spark"),
                amount = json.int("amount", 16, 0, common.limits.maxParticlesPerEffect, path, result),
                color = color,
                size = json.double("size", 0.25, 0.01, 8.0, path, result)
            )
        }
    }
}

data class MagicCircleComponent(
    val enabled: Boolean,
    val style: String = "arcane",
    val radius: Double = 2.0,
    val color: String = "#FFFFFFFF",
    val thickness: Double = 0.08,
    val segments: Int = 96,
    val facing: String = "face_camera",
    val rotationSpeed: Double = 1.0,
    val innerRadiusScale: Double = 0.68,
    val glyphs: Int = 12,
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): MagicCircleComponent {
            if (json == null) return MagicCircleComponent(enabled = false)
            return MagicCircleComponent(
                enabled = json.boolean("enabled", true),
                style = json.string("style", "arcane"),
                radius = json.double("radius", 2.0, 0.1, 32.0, path, result),
                color = parseColor(json.string("color", "#FFFFFFFF"), path, result),
                thickness = json.double("thickness", 0.08, 0.01, 2.0, path, result),
                segments = json.int("segments", 96, 16, 256, path, result),
                facing = json.string("facing", "face_camera"),
                rotationSpeed = json.double("rotationSpeed", 1.0, -16.0, 16.0, path, result),
                innerRadiusScale = json.double("innerRadiusScale", 0.68, 0.1, 0.95, path, result),
                glyphs = json.int("glyphs", 12, 0, 64, path, result),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class AuraComponent(
    val enabled: Boolean,
    val style: String = "soft",
    val radius: Double = 1.5,
    val color: String = "#FFFFFFFF"
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): AuraComponent {
            if (json == null) return AuraComponent(enabled = false)
            return AuraComponent(
                enabled = json.boolean("enabled", true),
                style = json.string("style", "soft"),
                radius = json.double("radius", 1.5, 0.1, 16.0, path, result),
                color = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            )
        }
    }
}

data class BeamComponent(
    val enabled: Boolean,
    val style: String = "mana",
    val width: Double = 0.15,
    val color: String = "#FFFFFFFF",
    val colorStart: String = "#FFFFFFFF",
    val colorEnd: String = "#FFFFFFFF",
    val segments: Int = 1,
    val noise: Double = 0.0,
    val texture: String = "magicrender:textures/effect/beam.png",
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): BeamComponent {
            if (json == null) return BeamComponent(enabled = false)
            val colorJson = json.obj("color")
            val fallbackColor = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            return BeamComponent(
                enabled = json.boolean("enabled", true),
                style = json.string("style", "mana"),
                width = json.double("width", 0.15, 0.01, 4.0, path, result),
                color = fallbackColor,
                colorStart = parseColor(colorJson?.string("start", fallbackColor) ?: fallbackColor, "$path.color", result),
                colorEnd = parseColor(colorJson?.string("end", fallbackColor) ?: fallbackColor, "$path.color", result),
                segments = json.int("segments", 1, 1, 64, path, result),
                noise = json.double("noise", 0.0, 0.0, 4.0, path, result),
                texture = validResourceOrDefault(json.string("texture", "magicrender:textures/effect/beam.png"), "magicrender:textures/effect/beam.png", "$path.texture", result),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class TrailMotionComponent(
    val mode: String = "follow",
    val radius: Double = 0.0,
    val angularSpeed: Double = 0.0,
    val verticalAmplitude: Double = 0.0,
    val verticalSpeed: Double = 0.0,
    val phase: Double = 0.0,
    val formula: TrailMotionFormulaComponent = TrailMotionFormulaComponent()
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): TrailMotionComponent {
            if (json == null) return TrailMotionComponent()
            return TrailMotionComponent(
                mode = json.string("mode", "follow"),
                radius = json.double("radius", 0.0, 0.0, 16.0, path, result),
                angularSpeed = json.double("angularSpeed", 0.0, -72.0, 72.0, path, result),
                verticalAmplitude = json.double("verticalAmplitude", 0.0, 0.0, 16.0, path, result),
                verticalSpeed = json.double("verticalSpeed", 0.0, -72.0, 72.0, path, result),
                phase = json.double("phase", 0.0, -360.0, 360.0, path, result),
                formula = TrailMotionFormulaComponent.parse(json.obj("formula"))
            )
        }
    }
}

data class TrailMotionFormulaComponent(
    val x: String = "",
    val y: String = "",
    val z: String = ""
) {
    companion object {
        fun parse(json: JsonObject?): TrailMotionFormulaComponent {
            if (json == null) return TrailMotionFormulaComponent()
            return TrailMotionFormulaComponent(
                x = json.string("x", ""),
                y = json.string("y", ""),
                z = json.string("z", "")
            )
        }
    }
}

data class TrailComponent(
    val enabled: Boolean,
    val style: String = "ribbon",
    val width: Double = 0.25,
    val color: String = "#FFFFFFFF",
    val colorStart: String = "#FFFFFFFF",
    val colorEnd: String = "#FFFFFFFF",
    val widthStart: Double = 0.25,
    val widthEnd: Double = 0.0,
    val lifetimeTicks: Int = 20,
    val maxPoints: Int = 32,
    val minSampleDistance: Double = 0.05,
    val maxSegmentLength: Double = 0.5,
    val maxInsertedPointsPerTick: Int = 4,
    val sampleEveryTick: Boolean = true,
    val renderMode: String = "face_camera",
    val texture: String = "minecraft:textures/particle/flame.png",
    val blendMode: String = "additive",
    val motion: TrailMotionComponent = TrailMotionComponent()
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): TrailComponent {
            if (json == null) return TrailComponent(enabled = false)
            val widthJson = json.obj("width")
            val colorJson = json.obj("color")
            val fallbackWidth = json.double("width", 0.25, 0.01, 8.0, path, result)
            val fallbackColor = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            return TrailComponent(
                enabled = json.boolean("enabled", true),
                style = json.string("style", "ribbon"),
                width = fallbackWidth,
                color = fallbackColor,
                colorStart = parseColor(colorJson?.string("start", fallbackColor) ?: fallbackColor, "$path.color", result),
                colorEnd = parseColor(colorJson?.string("end", fallbackColor) ?: fallbackColor, "$path.color", result),
                widthStart = widthJson?.double("start", fallbackWidth, 0.01, 8.0, "$path.width", result) ?: fallbackWidth,
                widthEnd = widthJson?.double("end", 0.0, 0.0, 8.0, "$path.width", result) ?: 0.0,
                lifetimeTicks = json.int("lifetimeTicks", 20, 1, 200, path, result),
                maxPoints = json.int("maxPoints", 32, 2, 128, path, result),
                minSampleDistance = json.double("minSampleDistance", 0.05, 0.01, 4.0, path, result),
                maxSegmentLength = json.double("maxSegmentLength", 0.5, 0.05, 8.0, path, result),
                maxInsertedPointsPerTick = json.int("maxInsertedPointsPerTick", 4, 0, 8, path, result),
                sampleEveryTick = json.boolean("sampleEveryTick", true),
                renderMode = json.string("renderMode", "face_camera"),
                texture = validResourceOrDefault(json.string("texture", "minecraft:textures/particle/flame.png"), "minecraft:textures/particle/flame.png", "$path.texture", result),
                blendMode = json.string("blendMode", "additive"),
                motion = TrailMotionComponent.parse(json.obj("motion"), "$path.motion", result)
            )
        }
    }
}

data class ShockwaveComponent(
    val enabled: Boolean,
    val strength: Double = 0.35
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): ShockwaveComponent {
            if (json == null) return ShockwaveComponent(enabled = false)
            return ShockwaveComponent(
                enabled = json.boolean("enabled", false),
                strength = json.double("strength", 0.35, 0.0, 1.0, path, result)
            )
        }
    }
}

data class AdvancedVisualComponent(
    val enabled: Boolean,
    val bloom: BloomApproximationComponent = BloomApproximationComponent(),
    val glow: GlowPostComponent = GlowPostComponent(),
    val core: CoreGlowComponent = CoreGlowComponent(enabled = false),
    val particleEmitters: List<ParticleEmitterComponent> = emptyList(),
    val ribbonBundles: List<RibbonBundleComponent> = emptyList(),
    val circleLayers: List<CircleLayerComponent> = emptyList(),
    val radialBursts: List<RadialBurstComponent> = emptyList()
) {
    companion object {
        fun parse(json: JsonObject?, common: CommonConfig, path: String, result: ConfigLoadAccumulator): AdvancedVisualComponent {
            if (json == null) return AdvancedVisualComponent(enabled = false)
            return AdvancedVisualComponent(
                enabled = json.boolean("enabled", true),
                bloom = BloomApproximationComponent.parse(json.obj("bloom"), "$path.bloom", result),
                glow = GlowPostComponent.parse(json.obj("glow"), "$path.glow", result),
                core = CoreGlowComponent.parse(json.obj("core"), "$path.core", result),
                particleEmitters = json.objectList("particleEmitters").take(8).mapIndexed { index, item ->
                    ParticleEmitterComponent.parse(item, common, "$path.particleEmitters[$index]", result)
                },
                ribbonBundles = json.objectList("ribbonBundles").take(8).mapIndexed { index, item ->
                    RibbonBundleComponent.parse(item, "$path.ribbonBundles[$index]", result)
                },
                circleLayers = json.objectList("circleLayers").take(12).mapIndexed { index, item ->
                    CircleLayerComponent.parse(item, "$path.circleLayers[$index]", result)
                },
                radialBursts = json.objectList("radialBursts").take(8).mapIndexed { index, item ->
                    RadialBurstComponent.parse(item, "$path.radialBursts[$index]", result)
                }
            )
        }
    }
}

data class GlowPostComponent(
    val enabled: Boolean = true,
    val intensity: Double = 1.35,
    val radius: Double = 1.0,
    val iterations: Int = 4,
    val downsample: Int = 2,
    val threshold: Double = 0.0
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): GlowPostComponent {
            if (json == null) return GlowPostComponent()
            return GlowPostComponent(
                enabled = json.boolean("enabled", true),
                intensity = json.double("intensity", 1.35, 0.0, 8.0, path, result),
                radius = json.double("radius", 1.0, 0.25, 8.0, path, result),
                iterations = json.int("iterations", 4, 1, 12, path, result),
                downsample = json.int("downsample", 2, 1, 8, path, result),
                threshold = json.double("threshold", 0.0, 0.0, 1.0, path, result)
            )
        }
    }
}

data class BloomApproximationComponent(
    val enabled: Boolean = true,
    val layers: Int = 3,
    val scaleStep: Double = 1.8,
    val alphaFalloff: Double = 0.45
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): BloomApproximationComponent {
            if (json == null) return BloomApproximationComponent()
            return BloomApproximationComponent(
                enabled = json.boolean("enabled", true),
                layers = json.int("layers", 3, 0, 8, path, result),
                scaleStep = json.double("scaleStep", 1.8, 1.0, 6.0, path, result),
                alphaFalloff = json.double("alphaFalloff", 0.45, 0.05, 1.0, path, result)
            )
        }
    }
}

data class CoreGlowComponent(
    val enabled: Boolean,
    val color: String = "#FFFFFFFF",
    val radius: Double = 0.6,
    val pulseAmplitude: Double = 0.18,
    val pulseSpeed: Double = 0.12,
    val texture: String = "minecraft:textures/particle/flash.png",
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): CoreGlowComponent {
            if (json == null) return CoreGlowComponent(enabled = false)
            return CoreGlowComponent(
                enabled = json.boolean("enabled", true),
                color = parseColor(json.string("color", "#FFFFFFFF"), path, result),
                radius = json.double("radius", 0.6, 0.01, 16.0, path, result),
                pulseAmplitude = json.double("pulseAmplitude", 0.18, 0.0, 4.0, path, result),
                pulseSpeed = json.double("pulseSpeed", 0.12, -4.0, 4.0, path, result),
                texture = validResourceOrDefault(json.string("texture", "minecraft:textures/particle/flash.png"), "minecraft:textures/particle/flash.png", "$path.texture", result),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class ParticleEmitterComponent(
    val enabled: Boolean = true,
    val shape: String = "sphere",
    val count: Int = 96,
    val colorStart: String = "#FFFFFFFF",
    val colorEnd: String = "#FFFFFFFF",
    val sizeStart: Double = 0.08,
    val sizeEnd: Double = 0.02,
    val radius: Double = 1.2,
    val height: Double = 2.0,
    val speed: Double = 0.02,
    val swirlSpeed: Double = 0.0,
    val noise: Double = 0.2,
    val texture: String = "minecraft:textures/particle/flash.png",
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject, common: CommonConfig, path: String, result: ConfigLoadAccumulator): ParticleEmitterComponent {
            val colorJson = json.obj("color")
            val sizeJson = json.obj("size")
            val fallbackColor = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            val fallbackSize = json.double("size", 0.08, 0.005, 4.0, path, result)
            return ParticleEmitterComponent(
                enabled = json.boolean("enabled", true),
                shape = json.string("shape", "sphere"),
                count = json.int("count", 96, 0, common.limits.maxParticlesPerEffect, path, result),
                colorStart = parseColor(colorJson?.string("start", fallbackColor) ?: json.string("colorStart", fallbackColor), "$path.color", result),
                colorEnd = parseColor(colorJson?.string("end", fallbackColor) ?: json.string("colorEnd", fallbackColor), "$path.color", result),
                sizeStart = sizeJson?.double("start", fallbackSize, 0.005, 4.0, "$path.size", result) ?: json.double("sizeStart", fallbackSize, 0.005, 4.0, path, result),
                sizeEnd = sizeJson?.double("end", fallbackSize * 0.35, 0.0, 4.0, "$path.size", result) ?: json.double("sizeEnd", fallbackSize * 0.35, 0.0, 4.0, path, result),
                radius = json.double("radius", 1.2, 0.0, 64.0, path, result),
                height = json.double("height", 2.0, 0.0, 64.0, path, result),
                speed = json.double("speed", 0.02, -4.0, 4.0, path, result),
                swirlSpeed = json.double("swirlSpeed", 0.0, -16.0, 16.0, path, result),
                noise = json.double("noise", 0.2, 0.0, 16.0, path, result),
                texture = validResourceOrDefault(json.string("texture", "minecraft:textures/particle/flash.png"), "minecraft:textures/particle/flash.png", "$path.texture", result),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class RibbonBundleComponent(
    val enabled: Boolean = true,
    val count: Int = 8,
    val widthStart: Double = 0.12,
    val widthEnd: Double = 0.02,
    val colorStart: String = "#FFFFFFFF",
    val colorEnd: String = "#FFFFFFFF",
    val length: Double = 8.0,
    val samples: Int = 96,
    val phaseStep: Double = 24.0,
    val amplitude: Double = 0.8,
    val frequency: Double = 1.4,
    val twist: Double = 0.45,
    val flowSpeed: Double = 0.08,
    val texture: String = "minecraft:textures/particle/flame.png",
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject, path: String, result: ConfigLoadAccumulator): RibbonBundleComponent {
            val colorJson = json.obj("color")
            val widthJson = json.obj("width")
            val fallbackColor = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            val fallbackWidth = json.double("width", 0.12, 0.005, 8.0, path, result)
            return RibbonBundleComponent(
                enabled = json.boolean("enabled", true),
                count = json.int("count", 8, 0, 64, path, result),
                widthStart = widthJson?.double("start", fallbackWidth, 0.005, 8.0, "$path.width", result) ?: json.double("widthStart", fallbackWidth, 0.005, 8.0, path, result),
                widthEnd = widthJson?.double("end", fallbackWidth * 0.35, 0.0, 8.0, "$path.width", result) ?: json.double("widthEnd", fallbackWidth * 0.35, 0.0, 8.0, path, result),
                colorStart = parseColor(colorJson?.string("start", fallbackColor) ?: json.string("colorStart", fallbackColor), "$path.color", result),
                colorEnd = parseColor(colorJson?.string("end", fallbackColor) ?: json.string("colorEnd", fallbackColor), "$path.color", result),
                length = json.double("length", 8.0, 0.1, 128.0, path, result),
                samples = json.int("samples", 96, 2, 256, path, result),
                phaseStep = json.double("phaseStep", 24.0, -360.0, 360.0, path, result),
                amplitude = json.double("amplitude", 0.8, 0.0, 32.0, path, result),
                frequency = json.double("frequency", 1.4, 0.0, 32.0, path, result),
                twist = json.double("twist", 0.45, -16.0, 16.0, path, result),
                flowSpeed = json.double("flowSpeed", 0.08, -8.0, 8.0, path, result),
                texture = validResourceOrDefault(json.string("texture", "minecraft:textures/particle/flame.png"), "minecraft:textures/particle/flame.png", "$path.texture", result),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class CircleLayerComponent(
    val enabled: Boolean = true,
    val radius: Double = 2.0,
    val thickness: Double = 0.04,
    val color: String = "#FFFFFFFF",
    val segments: Int = 128,
    val rotationSpeed: Double = 1.0,
    val glyphs: Int = 0,
    val glyphMode: String = "ticks",
    val facing: String = "face_camera",
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject, path: String, result: ConfigLoadAccumulator): CircleLayerComponent {
            return CircleLayerComponent(
                enabled = json.boolean("enabled", true),
                radius = json.double("radius", 2.0, 0.01, 64.0, path, result),
                thickness = json.double("thickness", 0.04, 0.005, 4.0, path, result),
                color = parseColor(json.string("color", "#FFFFFFFF"), path, result),
                segments = json.int("segments", 128, 8, 512, path, result),
                rotationSpeed = json.double("rotationSpeed", 1.0, -32.0, 32.0, path, result),
                glyphs = json.int("glyphs", 0, 0, 256, path, result),
                glyphMode = json.string("glyphMode", "ticks"),
                facing = json.string("facing", "face_camera"),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class RadialBurstComponent(
    val enabled: Boolean = true,
    val rays: Int = 16,
    val length: Double = 2.8,
    val widthStart: Double = 0.08,
    val widthEnd: Double = 0.0,
    val colorStart: String = "#FFFFFFFF",
    val colorEnd: String = "#FFFFFF00",
    val rotationSpeed: Double = 0.0,
    val randomJitter: Double = 0.15,
    val texture: String = "minecraft:textures/particle/flash.png",
    val blendMode: String = "additive"
) {
    companion object {
        fun parse(json: JsonObject, path: String, result: ConfigLoadAccumulator): RadialBurstComponent {
            val colorJson = json.obj("color")
            val widthJson = json.obj("width")
            val fallbackColor = parseColor(json.string("color", "#FFFFFFFF"), path, result)
            val fallbackWidth = json.double("width", 0.08, 0.005, 8.0, path, result)
            return RadialBurstComponent(
                enabled = json.boolean("enabled", true),
                rays = json.int("rays", 16, 0, 256, path, result),
                length = json.double("length", 2.8, 0.0, 64.0, path, result),
                widthStart = widthJson?.double("start", fallbackWidth, 0.005, 8.0, "$path.width", result) ?: json.double("widthStart", fallbackWidth, 0.005, 8.0, path, result),
                widthEnd = widthJson?.double("end", 0.0, 0.0, 8.0, "$path.width", result) ?: json.double("widthEnd", 0.0, 0.0, 8.0, path, result),
                colorStart = parseColor(colorJson?.string("start", fallbackColor) ?: json.string("colorStart", fallbackColor), "$path.color", result),
                colorEnd = parseColor(colorJson?.string("end", "#FFFFFF00") ?: json.string("colorEnd", "#FFFFFF00"), "$path.color", result),
                rotationSpeed = json.double("rotationSpeed", 0.0, -32.0, 32.0, path, result),
                randomJitter = json.double("randomJitter", 0.15, 0.0, 1.0, path, result),
                texture = validResourceOrDefault(json.string("texture", "minecraft:textures/particle/flash.png"), "minecraft:textures/particle/flash.png", "$path.texture", result),
                blendMode = json.string("blendMode", "additive")
            )
        }
    }
}

data class SoundComponent(
    val enabled: Boolean,
    val id: String = "minecraft:block.amethyst_block.chime",
    val volume: Double = 0.6,
    val pitch: Double = 1.0
) {
    companion object {
        fun parse(json: JsonObject?, path: String, result: ConfigLoadAccumulator): SoundComponent {
            if (json == null) return SoundComponent(enabled = false)
            val id = json.string("id", "minecraft:block.amethyst_block.chime")
            if (!isValidIdentifier(id)) {
                result.warning(path, "Invalid sound id `$id`. Using minecraft:block.amethyst_block.chime.")
            }
            return SoundComponent(
                enabled = json.boolean("enabled", true),
                id = if (isValidIdentifier(id)) id else "minecraft:block.amethyst_block.chime",
                volume = json.double("volume", 0.6, 0.0, 4.0, path, result),
                pitch = json.double("pitch", 1.0, 0.1, 4.0, path, result)
            )
        }
    }
}

private fun parseColor(value: String, path: String, result: ConfigLoadAccumulator): String {
    if (isColorString(value)) return value
    result.warning(path, "Invalid color `$value`. Using #FFFFFFFF.")
    return "#FFFFFFFF"
}

private fun validResourceOrDefault(value: String, default: String, path: String, result: ConfigLoadAccumulator): String {
    if (isValidIdentifier(value)) return value
    result.warning(path, "Invalid resource id `$value`. Using $default.")
    return default
}
