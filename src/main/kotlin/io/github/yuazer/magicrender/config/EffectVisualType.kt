package io.github.yuazer.magicrender.config

enum class EffectVisualType {
    PARTICLES,
    TRAILS,
    BEAMS,
    AURAS,
    MAGIC_CIRCLES,
    WORLD_INDICATORS,
    SCREEN_EFFECTS,
    OFFSCREEN_COMPOSITION
}

fun CommonConfig.isVisualEnabled(type: EffectVisualType): Boolean {
    return when (type) {
        EffectVisualType.PARTICLES -> visuals.particles
        EffectVisualType.TRAILS -> visuals.trails
        EffectVisualType.BEAMS -> visuals.beams
        EffectVisualType.AURAS -> visuals.auras
        EffectVisualType.MAGIC_CIRCLES -> visuals.magicCircles
        EffectVisualType.WORLD_INDICATORS -> visuals.worldIndicators
        EffectVisualType.SCREEN_EFFECTS -> visuals.screenEffects
        EffectVisualType.OFFSCREEN_COMPOSITION -> visuals.offscreenComposition
    }
}

