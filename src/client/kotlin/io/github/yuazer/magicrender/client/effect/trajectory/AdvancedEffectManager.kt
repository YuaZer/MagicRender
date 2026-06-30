package io.github.yuazer.magicrender.client.effect.trajectory

import io.github.yuazer.magicrender.client.config.ClientEffectGate
import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.EffectVisualType
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.config.AdvancedVisualComponent
import net.minecraft.world.phys.Vec3
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.sin

object AdvancedEffectManager {
    private val nextHandle = AtomicLong(1L)
    private val effects = linkedMapOf<Long, AdvancedEffectInstance>()

    @Volatile
    var lastFrameBillboards: List<BillboardMesh> = emptyList()
        private set

    @Volatile
    var lastFrameRibbons: List<RibbonMesh> = emptyList()
        private set

    @Volatile
    var lastFrameCircles: List<MagicCircleMesh> = emptyList()
        private set

    fun spawn(effectId: String, source: TrailAnchor, target: TrailAnchor? = null): Long? {
        val shared = MagicRenderConfigManager.current
        val effect = shared.effects[effectId] ?: return null
        return spawn(effect, source, target)
    }

    fun spawn(effect: EffectDefinition, source: TrailAnchor, target: TrailAnchor? = null): Long? {
        val shared = MagicRenderConfigManager.current
        if (!effect.components.advanced.enabled) return null
        if (!ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.PARTICLES) &&
            !ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.TRAILS) &&
            !ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.MAGIC_CIRCLES)
        ) return null

        val filtered = filterAllowed(effect)
        if (!filtered.core.enabled && filtered.particleEmitters.isEmpty() && filtered.ribbonBundles.isEmpty() && filtered.radialBursts.isEmpty() && filtered.circleLayers.isEmpty()) return null

        val handle = nextHandle.getAndIncrement()
        effects[handle] = AdvancedEffectInstance(
            handle = handle,
            definition = AdvancedEffectDefinition.from(effect.id, filtered),
            source = source,
            target = target,
            lifetimeTicks = effect.durationTicks,
            seeds = List(512) { seed(handle.toDouble() + it * 19.19) }
        )
        return handle
    }

    private fun filterAllowed(effect: EffectDefinition): AdvancedVisualComponent {
        val shared = MagicRenderConfigManager.current
        val component = effect.components.advanced
        val particlesAllowed = ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.PARTICLES)
        val trailsAllowed = ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.TRAILS)
        val circlesAllowed = ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.MAGIC_CIRCLES)
        return component.copy(
            core = if (particlesAllowed) component.core else component.core.copy(enabled = false),
            particleEmitters = if (particlesAllowed) component.particleEmitters else emptyList(),
            ribbonBundles = if (trailsAllowed) component.ribbonBundles else emptyList(),
            radialBursts = if (trailsAllowed) component.radialBursts else emptyList(),
            circleLayers = if (circlesAllowed) component.circleLayers else emptyList()
        )
    }

    fun stop(handle: Long): Boolean {
        return effects.remove(handle) != null
    }

    fun isActive(handle: Long): Boolean {
        return effects.containsKey(handle)
    }

    fun tick() {
        effects.values.forEach { it.ageTicks += 1 }
        effects.entries.removeIf { (_, effect) ->
            !effect.isAlive() || TrailAnchorResolver.resolve(effect.source) == null
        }
    }

    fun prepareFrame(cameraPosition: Vec3) {
        val billboards = ArrayList<BillboardMesh>()
        val ribbons = ArrayList<RibbonMesh>()
        val circles = ArrayList<MagicCircleMesh>()
        for (effect in effects.values) {
            val source = TrailAnchorResolver.resolve(effect.source) ?: continue
            val target = effect.target?.let(TrailAnchorResolver::resolve)
            billboards += AdvancedMeshBuilder.buildBillboards(effect, source, target, cameraPosition)
            ribbons += AdvancedMeshBuilder.buildRibbons(effect, source, target, cameraPosition)
            circles += AdvancedMeshBuilder.buildCircles(effect, source, cameraPosition)
        }
        lastFrameBillboards = billboards
        lastFrameRibbons = ribbons
        lastFrameCircles = circles
    }

    fun clear() {
        effects.clear()
        lastFrameBillboards = emptyList()
        lastFrameRibbons = emptyList()
        lastFrameCircles = emptyList()
    }

    private fun seed(value: Double): Double {
        val x = sin(value * 12.9898) * 43758.5453
        return x - kotlin.math.floor(x)
    }
}
