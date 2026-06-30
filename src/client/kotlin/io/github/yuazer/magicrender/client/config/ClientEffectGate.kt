package io.github.yuazer.magicrender.client.config

import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.EffectVisualType
import io.github.yuazer.magicrender.config.LoadedMagicRenderConfig
import io.github.yuazer.magicrender.config.isVisualEnabled

object ClientEffectGate {
    fun canUseEffect(shared: LoadedMagicRenderConfig, effect: EffectDefinition, type: EffectVisualType): Boolean {
        if (!shared.common.enabled || !shared.server.enabled || !effect.enabled) return false
        if (!shared.isGroupEnabled(effect.group)) return false
        if (!shared.common.isVisualEnabled(type)) return false
        if (!isClientVisualEnabled(type)) return false
        if (!isCompatibilityAllowed(type)) return false
        return true
    }

    private fun isClientVisualEnabled(type: EffectVisualType): Boolean {
        val visuals = ClientConfigReloader.current.visuals
        return when (type) {
            EffectVisualType.PARTICLES -> visuals.particles
            EffectVisualType.TRAILS -> visuals.trails
            EffectVisualType.BEAMS -> visuals.beams
            EffectVisualType.AURAS -> visuals.auras
            EffectVisualType.MAGIC_CIRCLES -> visuals.magicCircles
            EffectVisualType.WORLD_INDICATORS -> true
            EffectVisualType.SCREEN_EFFECTS -> visuals.screenDistortion ||
                visuals.screenGlow ||
                visuals.screenShockwaves ||
                visuals.chromaticShift ||
                visuals.blur
            EffectVisualType.OFFSCREEN_COMPOSITION -> true
        }
    }

    private fun isCompatibilityAllowed(type: EffectVisualType): Boolean {
        val compatibility = ClientConfigReloader.compatibility
        return when (type) {
            EffectVisualType.SCREEN_EFFECTS -> compatibility.screenEffectsAllowed
            EffectVisualType.OFFSCREEN_COMPOSITION -> compatibility.offscreenCompositionAllowed
            else -> true
        }
    }
}

