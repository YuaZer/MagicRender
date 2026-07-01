package io.github.yuazer.magicrender.client.effect.trajectory

import io.github.yuazer.magicrender.client.config.ClientEffectGate
import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.EffectVisualType
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import net.minecraft.world.phys.Vec3
import java.util.concurrent.atomic.AtomicLong

object MagicCircleManager {
    private val nextHandle = AtomicLong(1L)
    private val circles = linkedMapOf<Long, MagicCircleInstance>()

    @Volatile
    var lastFrameMeshes: List<MagicCircleMesh> = emptyList()
        private set

    fun spawn(effectId: String, anchor: TrailAnchor): Long? {
        val shared = MagicRenderConfigManager.current
        val effect = shared.effects[effectId] ?: return null
        return spawn(effect, anchor)
    }

    fun spawn(effect: EffectDefinition, anchor: TrailAnchor): Long? {
        val shared = MagicRenderConfigManager.current
        if (!effect.components.magicCircle.enabled) return null
        if (!ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.MAGIC_CIRCLES)) return null

        val handle = nextHandle.getAndIncrement()
        circles[handle] = MagicCircleInstance(
            handle = handle,
            definition = MagicCircleDefinition.from(effect.id, effect.components.magicCircle),
            anchor = anchor,
            lifetimeTicks = effect.durationTicks
        )
        return handle
    }

    fun stop(handle: Long): Boolean {
        return circles.remove(handle) != null
    }

    fun isActive(handle: Long): Boolean {
        return circles.containsKey(handle)
    }

    fun tick() {
        circles.values.forEach { it.ageTicks += 1 }
        circles.entries.removeIf { !it.value.isAlive() || TrailAnchorResolver.resolve(it.value.anchor) == null }
    }

    fun prepareFrame(context: TrajectoryRenderContext) {
        val meshes = ArrayList<MagicCircleMesh>(circles.size)
        for (circle in circles.values) {
            val center = TrailAnchorResolver.resolve(circle.anchor, context) ?: continue
            val mesh = MagicCircleMeshBuilder.build(circle, center, context.cameraPosition, circle.ageTicks.toDouble() + context.tickDelta.toDouble())
            if (mesh.vertices.isNotEmpty()) meshes += mesh
        }
        lastFrameMeshes = meshes
    }

    fun clear() {
        circles.clear()
        lastFrameMeshes = emptyList()
    }
}
