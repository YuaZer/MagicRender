package io.github.yuazer.magicrender.config

data class LoadedMagicRenderConfig(
    val common: CommonConfig = CommonConfig(),
    val server: ServerConfig = ServerConfig(),
    val groups: EffectGroupConfig = EffectGroupConfig(),
    val effects: Map<String, EffectDefinition> = emptyMap(),
    val lastResult: ConfigLoadResult = ConfigLoadResult.EMPTY
) {
    fun isGroupEnabled(groupName: String): Boolean {
        if (!common.groups.defaultEnabled) return false
        if (groupName in common.groups.disabledGroups) return false
        return groups.groupFor(groupName).enabled
    }

    fun isEffectEnabled(effectId: String): Boolean {
        val effect = effects[effectId] ?: return false
        return common.enabled && server.enabled && effect.enabled && isGroupEnabled(effect.group)
    }
}

