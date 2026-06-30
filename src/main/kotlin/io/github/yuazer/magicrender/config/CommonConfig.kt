package io.github.yuazer.magicrender.config

data class CommonConfig(
    val version: Int = 1,
    val enabled: Boolean = true,
    val visuals: CommonVisuals = CommonVisuals(),
    val limits: CommonLimits = CommonLimits(),
    val groups: CommonGroups = CommonGroups()
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject, result: ConfigLoadAccumulator, path: String): CommonConfig {
            val default = CommonConfig()
            json.warnUnknownFields(setOf("version", "enabled", "visuals", "limits", "groups"), path, result)
            val visualsJson = json.obj("visuals")
            val limitsJson = json.obj("limits")
            val groupsJson = json.obj("groups")
            return CommonConfig(
                version = json.int("version", default.version, 1, 1, path, result),
                enabled = json.boolean("enabled", default.enabled),
                visuals = CommonVisuals.parse(visualsJson, default.visuals),
                limits = CommonLimits.parse(limitsJson, default.limits, "$path.limits", result),
                groups = CommonGroups.parse(groupsJson, default.groups)
            )
        }
    }
}

data class CommonVisuals(
    val particles: Boolean = true,
    val trails: Boolean = true,
    val beams: Boolean = true,
    val auras: Boolean = true,
    val magicCircles: Boolean = true,
    val worldIndicators: Boolean = true,
    val screenEffects: Boolean = true,
    val offscreenComposition: Boolean = true
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, default: CommonVisuals): CommonVisuals {
            if (json == null) return default
            return CommonVisuals(
                particles = json.boolean("particles", default.particles),
                trails = json.boolean("trails", default.trails),
                beams = json.boolean("beams", default.beams),
                auras = json.boolean("auras", default.auras),
                magicCircles = json.boolean("magicCircles", default.magicCircles),
                worldIndicators = json.boolean("worldIndicators", default.worldIndicators),
                screenEffects = json.boolean("screenEffects", default.screenEffects),
                offscreenComposition = json.boolean("offscreenComposition", default.offscreenComposition)
            )
        }
    }
}

data class CommonLimits(
    val maxActiveEffects: Int = 256,
    val maxParticlesTotal: Int = 8000,
    val maxParticlesPerEffect: Int = 256,
    val maxTrails: Int = 128,
    val maxBeams: Int = 128,
    val defaultDrawDistance: Int = 64,
    val importantDrawDistance: Int = 128
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, default: CommonLimits, path: String, result: ConfigLoadAccumulator): CommonLimits {
            if (json == null) return default
            return CommonLimits(
                maxActiveEffects = json.int("maxActiveEffects", default.maxActiveEffects, 0, 2048, path, result),
                maxParticlesTotal = json.int("maxParticlesTotal", default.maxParticlesTotal, 0, 50000, path, result),
                maxParticlesPerEffect = json.int("maxParticlesPerEffect", default.maxParticlesPerEffect, 0, 4096, path, result),
                maxTrails = json.int("maxTrails", default.maxTrails, 0, 1024, path, result),
                maxBeams = json.int("maxBeams", default.maxBeams, 0, 1024, path, result),
                defaultDrawDistance = json.int("defaultDrawDistance", default.defaultDrawDistance, 0, 256, path, result),
                importantDrawDistance = json.int("importantDrawDistance", default.importantDrawDistance, 0, 512, path, result)
            )
        }
    }
}

data class CommonGroups(
    val defaultEnabled: Boolean = true,
    val disabledGroups: Set<String> = emptySet()
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, default: CommonGroups): CommonGroups {
            if (json == null) return default
            return CommonGroups(
                defaultEnabled = json.boolean("defaultEnabled", default.defaultEnabled),
                disabledGroups = json.stringList("disabledGroups").toSet()
            )
        }
    }
}
