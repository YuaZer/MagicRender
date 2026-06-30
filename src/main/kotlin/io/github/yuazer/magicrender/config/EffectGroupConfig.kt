package io.github.yuazer.magicrender.config

data class EffectGroupConfig(
    val version: Int = 1,
    val groups: Map<String, EffectGroup> = mapOf(
        "default" to EffectGroup(description = "默认特效组", priority = 50),
        "combat" to EffectGroup(description = "战斗技能特效", priority = 100),
        "ambient" to EffectGroup(description = "环境氛围特效", priority = 20),
        "debug" to EffectGroup(enabled = false, description = "调试可视化", priority = 0)
    )
) {
    fun groupFor(name: String): EffectGroup {
        return groups[name] ?: groups["default"] ?: EffectGroup()
    }

    companion object {
        fun parse(json: com.google.gson.JsonObject, common: CommonConfig, result: ConfigLoadAccumulator, path: String): EffectGroupConfig {
            json.warnUnknownFields(setOf("version", "groups"), path, result)
            val version = json.int("version", 1, 1, 1, path, result)
            val groupJson = json.obj("groups")
            if (groupJson == null) {
                result.warning(path, "`groups` is missing. Using default groups.")
                return EffectGroupConfig(version = version)
            }

            val groups = linkedMapOf<String, EffectGroup>()
            for ((groupName, element) in groupJson.entrySet()) {
                if (!element.isJsonObject) {
                    result.warning("$path.groups.$groupName", "Group must be an object. Skipping.")
                    continue
                }
                groups[groupName] = EffectGroup.parse(element.asJsonObject, "$path.groups.$groupName", common, result)
            }

            if (!groups.containsKey("default")) {
                groups["default"] = EffectGroup(description = "默认特效组", priority = 50)
            }

            return EffectGroupConfig(version = version, groups = groups)
        }
    }
}

data class EffectGroup(
    val enabled: Boolean = true,
    val description: String = "",
    val priority: Int = 50,
    val limits: EffectGroupLimits = EffectGroupLimits()
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject, path: String, common: CommonConfig, result: ConfigLoadAccumulator): EffectGroup {
            json.warnUnknownFields(setOf("enabled", "description", "priority", "limits"), path, result)
            return EffectGroup(
                enabled = json.boolean("enabled", true),
                description = json.string("description", ""),
                priority = json.int("priority", 50, 0, 1000, path, result),
                limits = EffectGroupLimits.parse(json.obj("limits"), common, "$path.limits", result)
            )
        }
    }
}

data class EffectGroupLimits(
    val maxActiveEffects: Int = 64,
    val drawDistance: Int = 64
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, common: CommonConfig, path: String, result: ConfigLoadAccumulator): EffectGroupLimits {
            if (json == null) return EffectGroupLimits(drawDistance = common.limits.defaultDrawDistance)
            return EffectGroupLimits(
                maxActiveEffects = json.int("maxActiveEffects", 64, 0, common.limits.maxActiveEffects, path, result),
                drawDistance = json.int("drawDistance", common.limits.defaultDrawDistance, 0, common.limits.importantDrawDistance, path, result)
            )
        }
    }
}
