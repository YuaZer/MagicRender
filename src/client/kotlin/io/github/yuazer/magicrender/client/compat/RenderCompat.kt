package io.github.yuazer.magicrender.client.compat

import io.github.yuazer.magicrender.client.config.ClientConfig
import net.fabricmc.loader.api.FabricLoader

data class RenderCompatibilityState(
    val irisLoaded: Boolean,
    val sodiumLoaded: Boolean,
    val screenEffectsAllowed: Boolean,
    val offscreenCompositionAllowed: Boolean,
    val resolvedMode: String
)

object RenderCompat {
    fun evaluate(config: ClientConfig): RenderCompatibilityState {
        val loader = FabricLoader.getInstance()
        val irisLoaded = loader.isModLoaded("iris")
        val sodiumLoaded = loader.isModLoaded("sodium")
        val requestedMode = config.compatibility.mode.lowercase()
        val resolvedMode = when {
            requestedMode == "safe" -> "safe"
            requestedMode == "experimental" -> "experimental"
            requestedMode == "balanced" -> "balanced"
            requestedMode == "normal" -> "normal"
            irisLoaded && config.compatibility.whenIrisLoaded.lowercase() == "safe" -> "safe"
            sodiumLoaded && config.compatibility.whenSodiumLoaded.lowercase() == "safe" -> "safe"
            else -> "auto"
        }

        val screenAllowedByMode = resolvedMode != "safe"
        val offscreenAllowedByMode = resolvedMode != "safe"

        return RenderCompatibilityState(
            irisLoaded = irisLoaded,
            sodiumLoaded = sodiumLoaded,
            screenEffectsAllowed = screenAllowedByMode && !(irisLoaded && config.compatibility.disableScreenEffectsWithShaders),
            offscreenCompositionAllowed = offscreenAllowedByMode && !(irisLoaded && config.compatibility.disableOffscreenCompositionWithShaders),
            resolvedMode = resolvedMode
        )
    }
}
