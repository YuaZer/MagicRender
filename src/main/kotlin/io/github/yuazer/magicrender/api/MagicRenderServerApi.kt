package io.github.yuazer.magicrender.api

import io.github.yuazer.magicrender.network.MagicRenderAnchorPayload
import io.github.yuazer.magicrender.network.MagicRenderPlayMode
import io.github.yuazer.magicrender.network.MagicRenderStopMode
import io.github.yuazer.magicrender.network.PlayEffectPayload
import io.github.yuazer.magicrender.network.StopEffectPayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import java.util.concurrent.atomic.AtomicLong

object MagicRenderServerApi {
    private val nextRequestId = AtomicLong(1L)

    @JvmStatic
    fun play(player: ServerPlayer, effectId: String, source: Entity, mode: MagicRenderPlayMode = MagicRenderPlayMode.EFFECT): Long {
        return sendPlay(player, effectId, mode, MagicRenderAnchorPayload.entity(source.id, defaultOffset(source)), null)
    }

    @JvmStatic
    fun play(player: ServerPlayer, effectId: String, source: Vec3, mode: MagicRenderPlayMode = MagicRenderPlayMode.EFFECT): Long {
        return sendPlay(player, effectId, mode, MagicRenderAnchorPayload.world(source), null)
    }

    @JvmStatic
    fun play(player: ServerPlayer, effectId: String, source: Entity, target: Entity, mode: MagicRenderPlayMode = MagicRenderPlayMode.EFFECT): Long {
        return sendPlay(
            player,
            effectId,
            mode,
            MagicRenderAnchorPayload.entity(source.id, defaultOffset(source)),
            MagicRenderAnchorPayload.entity(target.id, defaultOffset(target))
        )
    }

    @JvmStatic
    fun play(player: ServerPlayer, effectId: String, source: Vec3, target: Vec3, mode: MagicRenderPlayMode = MagicRenderPlayMode.EFFECT): Long {
        return sendPlay(player, effectId, mode, MagicRenderAnchorPayload.world(source), MagicRenderAnchorPayload.world(target))
    }

    @JvmStatic
    fun playForTracking(entity: Entity, effectId: String, mode: MagicRenderPlayMode = MagicRenderPlayMode.EFFECT): Long {
        val requestId = nextRequestId.getAndIncrement()
        val payload = PlayEffectPayload(
            requestId = requestId,
            effectId = effectId,
            mode = mode,
            source = MagicRenderAnchorPayload.entity(entity.id, defaultOffset(entity)),
            target = null
        )
        trackingPlayers(entity).forEach { sendIfPossible(it, payload) }
        return requestId
    }

    @JvmStatic
    fun broadcast(server: MinecraftServer, effectId: String, source: Vec3, mode: MagicRenderPlayMode = MagicRenderPlayMode.EFFECT): Long {
        val requestId = nextRequestId.getAndIncrement()
        val payload = PlayEffectPayload(requestId, effectId, mode, MagicRenderAnchorPayload.world(source), null)
        server.playerList.players.forEach { sendIfPossible(it, payload) }
        return requestId
    }

    @JvmStatic
    fun stop(player: ServerPlayer, requestId: Long) {
        sendStop(player, StopEffectPayload(requestId, MagicRenderStopMode.REQUEST, "", 0))
    }

    @JvmStatic
    fun stopEffect(player: ServerPlayer, effectId: String) {
        sendStop(player, StopEffectPayload(0L, MagicRenderStopMode.EFFECT_ID, effectId, 0))
    }

    @JvmStatic
    fun stopBoundToEntity(player: ServerPlayer, entity: Entity) {
        sendStop(player, StopEffectPayload(0L, MagicRenderStopMode.ENTITY_ID, "", entity.id))
    }

    @JvmStatic
    fun stopAll(player: ServerPlayer) {
        sendStop(player, StopEffectPayload(0L, MagicRenderStopMode.ALL, "", 0))
    }

    private fun sendPlay(
        player: ServerPlayer,
        effectId: String,
        mode: MagicRenderPlayMode,
        source: MagicRenderAnchorPayload,
        target: MagicRenderAnchorPayload?
    ): Long {
        val requestId = nextRequestId.getAndIncrement()
        sendIfPossible(player, PlayEffectPayload(requestId, effectId, mode, source, target))
        return requestId
    }

    private fun sendStop(player: ServerPlayer, payload: StopEffectPayload) {
        if (ServerPlayNetworking.canSend(player, StopEffectPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload)
        }
    }

    private fun sendIfPossible(player: ServerPlayer, payload: PlayEffectPayload) {
        if (ServerPlayNetworking.canSend(player, PlayEffectPayload.TYPE)) {
            ServerPlayNetworking.send(player, payload)
        }
    }

    private fun trackingPlayers(entity: Entity): List<ServerPlayer> {
        val serverLevel = entity.level()
        val server = serverLevel.server ?: return emptyList()
        val range = 128.0
        return server.playerList.players.filter { player ->
            player.level() == serverLevel && player.distanceToSqr(entity) <= range * range
        }
    }

    private fun defaultOffset(entity: Entity): Vec3 {
        return Vec3(0.0, entity.getBbHeight().toDouble() * 0.55, 0.0)
    }
}
