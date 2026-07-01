package io.github.yuazer.magicrender.client.effect.trajectory

import io.github.yuazer.magicrender.client.config.ClientEffectGate
import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.EffectVisualType
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import java.util.concurrent.atomic.AtomicLong

object MotionEffectManager {
    private val nextHandle = AtomicLong(1L)
    private val trails = linkedMapOf<Long, TrailEffectInstance>()
    private val beams = linkedMapOf<Long, BeamEffectInstance>()

    @Volatile
    var lastFrameMeshes: List<RibbonMesh> = emptyList()
        private set

    fun spawnTrail(effectId: String, anchor: TrailAnchor): Long? {
        val shared = MagicRenderConfigManager.current
        val effect = shared.effects[effectId] ?: return null
        return spawnTrail(effect, anchor)
    }

    fun spawnTrail(effect: EffectDefinition, anchor: TrailAnchor): Long? {
        val shared = MagicRenderConfigManager.current
        if (!effect.components.trail.enabled) return null
        if (!ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.TRAILS)) return null
        if (trails.size >= shared.common.limits.maxTrails) return null

        val handle = nextHandle.getAndIncrement()
        val definition = TrailDefinition.from(effect.id, effect.components.trail)
        trails[handle] = TrailEffectInstance(
            handle = handle,
            definition = definition,
            anchor = applyMotion(anchor, definition.motion),
            lifetimeTicks = effect.durationTicks
        )
        return handle
    }

    fun spawnBeam(effectId: String, from: TrailAnchor, to: TrailAnchor): Long? {
        val shared = MagicRenderConfigManager.current
        val effect = shared.effects[effectId] ?: return null
        return spawnBeam(effect, from, to)
    }

    fun spawnBeam(effect: EffectDefinition, from: TrailAnchor, to: TrailAnchor): Long? {
        val shared = MagicRenderConfigManager.current
        if (!effect.components.beam.enabled) return null
        if (!ClientEffectGate.canUseEffect(shared, effect, EffectVisualType.BEAMS)) return null
        if (beams.size >= shared.common.limits.maxBeams) return null

        val handle = nextHandle.getAndIncrement()
        beams[handle] = BeamEffectInstance(
            handle = handle,
            definition = BeamDefinition.from(effect.id, effect.components.beam),
            from = from,
            to = to,
            lifetimeTicks = effect.durationTicks
        )
        return handle
    }

    fun stop(handle: Long): Boolean {
        var stopped = false
        trails[handle]?.let {
            it.state.enabled = false
            stopped = true
        }
        if (beams.remove(handle) != null) stopped = true
        return stopped
    }

    fun isActive(handle: Long): Boolean {
        return trails.containsKey(handle) || beams.containsKey(handle)
    }

    fun tick() {
        trails.values.forEach { trail ->
            trail.ageTicks += 1
            if (trail.ageTicks > trail.lifetimeTicks) {
                trail.state.enabled = false
            }
        }
        trails.entries.removeIf { !it.value.isAlive() }

        beams.values.forEach { it.ageTicks += 1 }
        beams.entries.removeIf { !it.value.isAlive() }
    }

    fun buildMeshes(cameraPosition: Vec3 = Minecraft.getInstance().gameRenderer.mainCamera.position): List<RibbonMesh> {
        val context = TrajectoryRenderContext(cameraPosition, 1.0f, (Minecraft.getInstance().level?.gameTime ?: 0L).toDouble(), System.nanoTime())
        return buildMeshes(context)
    }

    fun buildMeshes(context: TrajectoryRenderContext): List<RibbonMesh> {
        val meshes = ArrayList<RibbonMesh>(trails.size + beams.size)
        for (trail in trails.values) {
            TrailSampler.sampleFrame(trail, context)
            val mesh = RibbonMeshBuilder.buildTrail(trail, context.cameraPosition)
            if (mesh.vertices.isNotEmpty()) meshes += mesh
        }
        for (beam in beams.values) {
            val points = BeamPointGenerator.generate(beam, context)
            val mesh = RibbonMeshBuilder.buildBeam(beam, points, context.cameraPosition)
            if (mesh.vertices.isNotEmpty()) meshes += mesh
        }
        return meshes
    }

    fun prepareFrame(context: TrajectoryRenderContext) {
        lastFrameMeshes = buildMeshes(context)
    }

    fun clear() {
        trails.clear()
        beams.clear()
        lastFrameMeshes = emptyList()
    }

    private fun applyMotion(anchor: TrailAnchor, motion: TrailMotionDefinition): TrailAnchor {
        if (motion.mode == TrailMotionMode.FOLLOW) return anchor
        return when (anchor) {
            is TrailAnchor.Entity -> TrailAnchor.EntityMotion(anchor.entityId, anchor.offset, motion)
            else -> anchor
        }
    }

    fun stats(): MotionEffectStats {
        return MotionEffectStats(
            activeTrails = trails.size,
            activeBeams = beams.size,
            trailPoints = trails.values.sumOf { it.state.points.size }
        )
    }
}

data class MotionEffectStats(
    val activeTrails: Int,
    val activeBeams: Int,
    val trailPoints: Int
)
