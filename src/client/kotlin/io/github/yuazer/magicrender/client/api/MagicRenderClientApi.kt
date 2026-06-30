package io.github.yuazer.magicrender.client.api

import io.github.yuazer.magicrender.client.effect.trajectory.AdvancedEffectManager
import io.github.yuazer.magicrender.client.effect.trajectory.MagicCircleManager
import io.github.yuazer.magicrender.client.effect.trajectory.MotionEffectManager
import io.github.yuazer.magicrender.client.effect.trajectory.TrailAnchor
import io.github.yuazer.magicrender.config.EffectDefinition
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.config.isValidIdentifier
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.concurrent.atomic.AtomicLong

/**
 * Public client-side API for spawning and stopping MagicRender effects.
 *
 * All methods are intended for client-side use. Server-side callers need a
 * networking layer that tells each client which effect to spawn.
 */
object MagicRenderClientApi {
    private val nextApiHandle = AtomicLong(1L)
    private val sessions = linkedMapOf<Long, MagicRenderEffectSession>()

    @JvmStatic
    @JvmOverloads
    fun playEffect(effectId: String, source: Entity, yOffsetScale: Double = 0.55): Long {
        return playEffect(effectId, entityAnchor(source, yOffsetScale))
    }

    @JvmStatic
    fun playEffect(effectId: String, source: Vec3): Long {
        return playEffect(effectId, TrailAnchor.WorldPoint(source))
    }

    @JvmStatic
    fun playEffect(effectId: String, source: TrailAnchor): Long {
        val effect = effect(effectId) ?: return NO_HANDLE
        return playEffect(effect, source)
    }

    @JvmStatic
    fun playEffect(effect: EffectDefinition, source: TrailAnchor): Long {
        if (!effect.enabled) return NO_HANDLE
        val parts = mutableListOf<MagicRenderComponentHandle>()
        MotionEffectManager.spawnTrail(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.TRAIL, it) }
        MagicCircleManager.spawn(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.MAGIC_CIRCLE, it) }
        AdvancedEffectManager.spawn(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.ADVANCED, it) }
        return register(effect.id, source.entityIdOrNull(), parts)
    }

    @JvmStatic
    @JvmOverloads
    fun playEffect(effectId: String, source: Entity, target: Entity, sourceYOffsetScale: Double = 0.55, targetYOffsetScale: Double = 0.55): Long {
        return playEffect(effectId, entityAnchor(source, sourceYOffsetScale), entityAnchor(target, targetYOffsetScale))
    }

    @JvmStatic
    fun playEffect(effectId: String, source: Vec3, target: Vec3): Long {
        return playEffect(effectId, TrailAnchor.WorldPoint(source), TrailAnchor.WorldPoint(target))
    }

    @JvmStatic
    fun playEffect(effectId: String, source: TrailAnchor, target: TrailAnchor): Long {
        val effect = effect(effectId) ?: return NO_HANDLE
        return playEffect(effect, source, target)
    }

    @JvmStatic
    fun playEffect(effect: EffectDefinition, source: TrailAnchor, target: TrailAnchor): Long {
        if (!effect.enabled) return NO_HANDLE
        val parts = mutableListOf<MagicRenderComponentHandle>()
        MotionEffectManager.spawnTrail(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.TRAIL, it) }
        MagicCircleManager.spawn(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.MAGIC_CIRCLE, it) }
        MotionEffectManager.spawnBeam(effect, source, target)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.BEAM, it) }
        AdvancedEffectManager.spawn(effect, source, target)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.ADVANCED, it) }
        return register(effect.id, source.entityIdOrNull(), parts)
    }

    @JvmStatic
    @JvmOverloads
    fun playTrail(effectId: String, source: Entity, yOffsetScale: Double = 0.55): Long {
        return playTrail(effectId, entityAnchor(source, yOffsetScale))
    }

    @JvmStatic
    fun playTrail(effectId: String, source: TrailAnchor): Long {
        val effect = effect(effectId) ?: return NO_HANDLE
        return playTrail(effect, source)
    }

    @JvmStatic
    fun playTrail(effect: EffectDefinition, source: TrailAnchor): Long {
        if (!effect.enabled) return NO_HANDLE
        val handle = MotionEffectManager.spawnTrail(effect, source) ?: return NO_HANDLE
        return register(effect.id, source.entityIdOrNull(), listOf(MagicRenderComponentHandle(MagicRenderComponentType.TRAIL, handle)))
    }

    @JvmStatic
    fun playMagicCircle(effectId: String, source: Entity, yOffsetScale: Double = 0.55): Long {
        return playMagicCircle(effectId, entityAnchor(source, yOffsetScale))
    }

    @JvmStatic
    fun playMagicCircle(effectId: String, source: TrailAnchor): Long {
        val effect = effect(effectId) ?: return NO_HANDLE
        return playMagicCircle(effect, source)
    }

    @JvmStatic
    fun playMagicCircle(effect: EffectDefinition, source: TrailAnchor): Long {
        if (!effect.enabled) return NO_HANDLE
        val handle = MagicCircleManager.spawn(effect, source) ?: return NO_HANDLE
        return register(effect.id, source.entityIdOrNull(), listOf(MagicRenderComponentHandle(MagicRenderComponentType.MAGIC_CIRCLE, handle)))
    }

    @JvmStatic
    @JvmOverloads
    fun playBeam(effectId: String, source: Entity, target: Entity, sourceYOffsetScale: Double = 0.55, targetYOffsetScale: Double = 0.55): Long {
        return playBeam(effectId, entityAnchor(source, sourceYOffsetScale), entityAnchor(target, targetYOffsetScale))
    }

    @JvmStatic
    fun playBeam(effectId: String, source: Vec3, target: Vec3): Long {
        return playBeam(effectId, TrailAnchor.WorldPoint(source), TrailAnchor.WorldPoint(target))
    }

    @JvmStatic
    fun playBeam(effectId: String, source: TrailAnchor, target: TrailAnchor): Long {
        val effect = effect(effectId) ?: return NO_HANDLE
        return playBeam(effect, source, target)
    }

    @JvmStatic
    fun playBeam(effect: EffectDefinition, source: TrailAnchor, target: TrailAnchor): Long {
        if (!effect.enabled) return NO_HANDLE
        val handle = MotionEffectManager.spawnBeam(effect, source, target) ?: return NO_HANDLE
        return register(effect.id, source.entityIdOrNull(), listOf(MagicRenderComponentHandle(MagicRenderComponentType.BEAM, handle)))
    }

    @JvmStatic
    @JvmOverloads
    fun bindEntity(effectId: String, entity: Entity, yOffsetScale: Double = 0.55): Long {
        return playEffect(effectId, entity, yOffsetScale)
    }

    @JvmStatic
    @JvmOverloads
    fun bindEntityTrail(effectId: String, entity: Entity, yOffsetScale: Double = 0.55): Long {
        return playTrail(effectId, entity, yOffsetScale)
    }

    @JvmStatic
    @JvmOverloads
    fun bindEntityMagicCircle(effectId: String, entity: Entity, yOffsetScale: Double = 0.55): Long {
        return playMagicCircle(effectId, entity, yOffsetScale)
    }

    @JvmStatic
    @JvmOverloads
    fun bindEntityStream(effectId: String, entity: Entity, yOffsetScale: Double = 0.55): Long {
        val source = entityAnchor(entity, yOffsetScale)
        val effect = effect(effectId) ?: return NO_HANDLE
        val parts = mutableListOf<MagicRenderComponentHandle>()
        MotionEffectManager.spawnTrail(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.TRAIL, it) }
        MagicCircleManager.spawn(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.MAGIC_CIRCLE, it) }
        AdvancedEffectManager.spawn(effect, source)?.let { parts += MagicRenderComponentHandle(MagicRenderComponentType.ADVANCED, it) }
        return register(effect.id, entity.id, parts)
    }

    @JvmStatic
    fun stop(handle: Long): Boolean {
        val session = sessions.remove(handle) ?: return false
        stopParts(session.parts)
        return true
    }

    @JvmStatic
    fun stopEffect(effectId: String): Int {
        pruneFinishedSessions()
        val matching = sessions.values
            .filter { it.effectId == effectId }
            .map { it.handle }
        matching.forEach(::stop)
        return matching.size
    }

    @JvmStatic
    fun stopBoundToEntity(entity: Entity): Int {
        return stopBoundToEntity(entity.id)
    }

    @JvmStatic
    fun stopBoundToEntity(entityId: Int): Int {
        pruneFinishedSessions()
        val matching = sessions.values
            .filter { it.sourceEntityId == entityId }
            .map { it.handle }
        matching.forEach(::stop)
        return matching.size
    }

    @JvmStatic
    fun stopRawComponentHandle(handle: Long) {
        val owningSessions = sessions.values
            .filter { session -> session.parts.any { it.handle == handle } }
            .map { it.handle }
        if (owningSessions.isNotEmpty()) {
            owningSessions.forEach(::stop)
            return
        }
        MotionEffectManager.stop(handle)
        MagicCircleManager.stop(handle)
        AdvancedEffectManager.stop(handle)
    }

    @JvmStatic
    fun stopAllApiEffects(): Int {
        pruneFinishedSessions()
        val handles = sessions.keys.toList()
        handles.forEach(::stop)
        return handles.size
    }

    @JvmStatic
    fun clearAllRenderedEffects() {
        sessions.clear()
        MotionEffectManager.clear()
        MagicCircleManager.clear()
        AdvancedEffectManager.clear()
    }

    @JvmStatic
    fun isPlaying(handle: Long): Boolean {
        pruneFinishedSessions()
        return sessions.containsKey(handle)
    }

    @JvmStatic
    fun activeSessions(): List<MagicRenderEffectSession> {
        pruneFinishedSessions()
        return sessions.values.toList()
    }

    @JvmStatic
    fun loadedEffect(effectId: String): EffectDefinition? {
        return effect(effectId)
    }

    @JvmStatic
    fun loadedEffectIds(): Set<String> {
        return MagicRenderConfigManager.current.effects.keys
    }

    @JvmStatic
    @JvmOverloads
    fun entityAnchor(entity: Entity, yOffsetScale: Double = 0.55): TrailAnchor.Entity {
        return TrailAnchor.Entity(entity.id, Vec3(0.0, entity.getBbHeight().toDouble() * yOffsetScale, 0.0))
    }

    @JvmStatic
    @JvmOverloads
    fun playerAnchor(yOffsetScale: Double = 0.55): TrailAnchor.Entity? {
        val player = Minecraft.getInstance().player ?: return null
        return entityAnchor(player, yOffsetScale)
    }

    @JvmStatic
    fun worldPoint(x: Double, y: Double, z: Double): TrailAnchor.WorldPoint {
        return TrailAnchor.WorldPoint(Vec3(x, y, z))
    }

    private fun effect(effectId: String): EffectDefinition? {
        if (!isValidIdentifier(effectId)) return null
        val effect = MagicRenderConfigManager.current.effects[effectId] ?: return null
        return if (effect.enabled) effect else null
    }

    private fun register(effectId: String, sourceEntityId: Int?, parts: List<MagicRenderComponentHandle>): Long {
        if (parts.isEmpty()) return NO_HANDLE
        val handle = nextApiHandle.getAndIncrement()
        sessions[handle] = MagicRenderEffectSession(
            handle = handle,
            effectId = effectId,
            sourceEntityId = sourceEntityId,
            parts = parts
        )
        return handle
    }

    private fun stopParts(parts: List<MagicRenderComponentHandle>) {
        for (part in parts) {
            when (part.type) {
                MagicRenderComponentType.TRAIL,
                MagicRenderComponentType.BEAM -> MotionEffectManager.stop(part.handle)
                MagicRenderComponentType.MAGIC_CIRCLE -> MagicCircleManager.stop(part.handle)
                MagicRenderComponentType.ADVANCED -> AdvancedEffectManager.stop(part.handle)
            }
        }
    }

    private fun pruneFinishedSessions() {
        sessions.entries.removeIf { (_, session) ->
            session.parts.none(::isPartActive)
        }
    }

    private fun isPartActive(part: MagicRenderComponentHandle): Boolean {
        return when (part.type) {
            MagicRenderComponentType.TRAIL,
            MagicRenderComponentType.BEAM -> MotionEffectManager.isActive(part.handle)
            MagicRenderComponentType.MAGIC_CIRCLE -> MagicCircleManager.isActive(part.handle)
            MagicRenderComponentType.ADVANCED -> AdvancedEffectManager.isActive(part.handle)
        }
    }

    private fun TrailAnchor.entityIdOrNull(): Int? {
        return when (this) {
            is TrailAnchor.Entity -> entityId
            is TrailAnchor.EntityMotion -> entityId
            is TrailAnchor.WorldPoint -> null
        }
    }

    const val NO_HANDLE: Long = 0L
}

data class MagicRenderEffectSession(
    val handle: Long,
    val effectId: String,
    val sourceEntityId: Int?,
    val parts: List<MagicRenderComponentHandle>
)

data class MagicRenderComponentHandle(
    val type: MagicRenderComponentType,
    val handle: Long
)

enum class MagicRenderComponentType {
    TRAIL,
    BEAM,
    MAGIC_CIRCLE,
    ADVANCED
}
