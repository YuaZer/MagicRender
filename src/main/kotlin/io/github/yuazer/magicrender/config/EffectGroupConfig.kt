package io.github.yuazer.magicrender.config

data class EffectGroupConfig(
    val version: Int = 1,
    val groups: Map<String, EffectGroup> = mapOf(
        "default" to EffectGroup(description = "Default effect group", priority = 50),
        "combat" to EffectGroup(description = "Combat skill effects", priority = 100),
        "ambient" to EffectGroup(description = "Ambient effects", priority = 20),
        "debug" to EffectGroup(enabled = false, description = "Debug visualization", priority = 0)
    ),
    val bindings: Map<String, List<String>> = emptyMap()
) {
    fun groupFor(name: String): EffectGroup {
        return groups[name] ?: groups["default"] ?: EffectGroup()
    }

    fun effectsFor(groupKey: String): List<String> {
        return bindings[groupKey].orEmpty()
    }

    fun bindingKeys(): Set<String> {
        return bindings.keys
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
            val bindings = linkedMapOf<String, List<String>>()
            for ((groupName, element) in groupJson.entrySet()) {
                when {
                    element.isJsonArray -> {
                        bindings[groupName] = parseEffectIdArray(element.asJsonArray, "$path.groups.$groupName", result)
                        groups.putIfAbsent(groupName, EffectGroup(description = "Effect binding group `$groupName`"))
                    }
                    element.isJsonObject -> {
                        val objectJson = element.asJsonObject
                        val effectIds = parseObjectEffectIds(objectJson, "$path.groups.$groupName", result)
                        if (effectIds.isNotEmpty()) {
                            bindings[groupName] = effectIds
                        }
                        groups[groupName] = EffectGroup.parse(objectJson, "$path.groups.$groupName", common, result)
                    }
                    else -> result.warning("$path.groups.$groupName", "Group must be an array of effect ids or an object. Skipping.")
                }
            }

            if (!groups.containsKey("default")) {
                groups["default"] = EffectGroup(description = "Default effect group", priority = 50)
            }

            return EffectGroupConfig(version = version, groups = groups, bindings = bindings)
        }

        private fun parseObjectEffectIds(json: com.google.gson.JsonObject, path: String, result: ConfigLoadAccumulator): List<String> {
            val element = json.get("effects") ?: json.get("effectIds") ?: return emptyList()
            if (!element.isJsonArray) {
                result.warning(path, "`effects` must be an array of effect ids. Ignoring.")
                return emptyList()
            }
            return parseEffectIdArray(element.asJsonArray, path, result)
        }

        private fun parseEffectIdArray(array: com.google.gson.JsonArray, path: String, result: ConfigLoadAccumulator): List<String> {
            return array.mapNotNull { item ->
                val id = item.asStringOrNull()
                when {
                    id == null -> {
                        result.warning(path, "Effect id must be a string. Skipping item.")
                        null
                    }
                    !isValidIdentifier(id) -> {
                        result.warning(path, "Invalid effect id `$id`. Skipping item.")
                        null
                    }
                    else -> id
                }
            }.distinct()
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
            json.warnUnknownFields(setOf("enabled", "description", "priority", "limits", "effects", "effectIds"), path, result)
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
