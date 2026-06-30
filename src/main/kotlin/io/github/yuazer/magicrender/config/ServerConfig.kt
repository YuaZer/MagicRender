package io.github.yuazer.magicrender.config

data class ServerConfig(
    val version: Int = 1,
    val enabled: Boolean = true,
    val sync: ServerSync = ServerSync(),
    val permissions: ServerPermissions = ServerPermissions(),
    val limits: ServerLimits = ServerLimits()
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject, result: ConfigLoadAccumulator, path: String): ServerConfig {
            val default = ServerConfig()
            json.warnUnknownFields(setOf("version", "enabled", "sync", "permissions", "limits"), path, result)
            return ServerConfig(
                version = json.int("version", default.version, 1, 1, path, result),
                enabled = json.boolean("enabled", default.enabled),
                sync = ServerSync.parse(json.obj("sync"), default.sync),
                permissions = ServerPermissions.parse(json.obj("permissions"), default.permissions, "$path.permissions", result),
                limits = ServerLimits.parse(json.obj("limits"), default.limits, "$path.limits", result)
            )
        }
    }
}

data class ServerSync(
    val sendEffectEventsToClients: Boolean = true,
    val sendEffectDefinitions: Boolean = false,
    val allowClientOverrides: Boolean = true
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, default: ServerSync): ServerSync {
            if (json == null) return default
            return ServerSync(
                sendEffectEventsToClients = json.boolean("sendEffectEventsToClients", default.sendEffectEventsToClients),
                sendEffectDefinitions = json.boolean("sendEffectDefinitions", default.sendEffectDefinitions),
                allowClientOverrides = json.boolean("allowClientOverrides", default.allowClientOverrides)
            )
        }
    }
}

data class ServerPermissions(
    val reloadRequiresLevel: Int = 2,
    val spawnTestEffectRequiresLevel: Int = 2
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, default: ServerPermissions, path: String, result: ConfigLoadAccumulator): ServerPermissions {
            if (json == null) return default
            return ServerPermissions(
                reloadRequiresLevel = json.int("reloadRequiresLevel", default.reloadRequiresLevel, 0, 4, path, result),
                spawnTestEffectRequiresLevel = json.int("spawnTestEffectRequiresLevel", default.spawnTestEffectRequiresLevel, 0, 4, path, result)
            )
        }
    }
}

data class ServerLimits(
    val maxEffectsPerPlayer: Int = 64,
    val maxBroadcastDistance: Int = 96,
    val rateLimitPerSecond: Int = 20
) {
    companion object {
        fun parse(json: com.google.gson.JsonObject?, default: ServerLimits, path: String, result: ConfigLoadAccumulator): ServerLimits {
            if (json == null) return default
            return ServerLimits(
                maxEffectsPerPlayer = json.int("maxEffectsPerPlayer", default.maxEffectsPerPlayer, 0, 512, path, result),
                maxBroadcastDistance = json.int("maxBroadcastDistance", default.maxBroadcastDistance, 0, 256, path, result),
                rateLimitPerSecond = json.int("rateLimitPerSecond", default.rateLimitPerSecond, 0, 200, path, result)
            )
        }
    }
}
