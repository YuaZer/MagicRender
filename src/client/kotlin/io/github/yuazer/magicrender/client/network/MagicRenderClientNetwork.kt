package io.github.yuazer.magicrender.client.network

import io.github.yuazer.magicrender.client.api.MagicRenderClientApi
import io.github.yuazer.magicrender.client.effect.trajectory.TrailAnchor
import io.github.yuazer.magicrender.network.MagicRenderAnchorPayload
import io.github.yuazer.magicrender.network.MagicRenderAnchorType
import io.github.yuazer.magicrender.network.MagicRenderPlayMode
import io.github.yuazer.magicrender.network.MagicRenderStopMode
import io.github.yuazer.magicrender.network.PlayEffectPayload
import io.github.yuazer.magicrender.network.StopEffectPayload
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3

object MagicRenderClientNetwork {
    private val requestHandles = linkedMapOf<Long, Long>()

    fun register() {
        ClientPlayNetworking.registerGlobalReceiver(PlayEffectPayload.TYPE) { payload, context ->
            context.client().execute {
                handlePlay(payload)
            }
        }
        ClientPlayNetworking.registerGlobalReceiver(StopEffectPayload.TYPE) { payload, context ->
            context.client().execute {
                handleStop(payload)
            }
        }
    }

    private fun handlePlay(payload: PlayEffectPayload) {
        val source = anchor(payload.source) ?: return
        val target = payload.target?.let(::anchor)
        val handle = when (payload.mode) {
            MagicRenderPlayMode.EFFECT -> if (target != null) {
                MagicRenderClientApi.playEffect(payload.effectId, source, target)
            } else {
                MagicRenderClientApi.playEffect(payload.effectId, source)
            }
            MagicRenderPlayMode.TRAIL -> MagicRenderClientApi.playTrail(payload.effectId, source)
            MagicRenderPlayMode.MAGIC_CIRCLE -> MagicRenderClientApi.playMagicCircle(payload.effectId, source)
            MagicRenderPlayMode.BEAM -> if (target != null) MagicRenderClientApi.playBeam(payload.effectId, source, target) else MagicRenderClientApi.NO_HANDLE
            MagicRenderPlayMode.STREAM -> MagicRenderClientApi.playEffect(payload.effectId, source)
        }
        if (handle != MagicRenderClientApi.NO_HANDLE) {
            requestHandles[payload.requestId] = handle
        }
    }

    private fun handleStop(payload: StopEffectPayload) {
        when (payload.stopMode) {
            MagicRenderStopMode.REQUEST -> {
                val handle = requestHandles.remove(payload.requestId) ?: return
                MagicRenderClientApi.stop(handle)
            }
            MagicRenderStopMode.EFFECT_ID -> {
                MagicRenderClientApi.stopEffect(payload.effectId)
                requestHandles.entries.removeIf { (_, handle) -> !MagicRenderClientApi.isPlaying(handle) }
            }
            MagicRenderStopMode.ENTITY_ID -> {
                MagicRenderClientApi.stopBoundToEntity(payload.entityId)
                requestHandles.entries.removeIf { (_, handle) -> !MagicRenderClientApi.isPlaying(handle) }
            }
            MagicRenderStopMode.ALL -> {
                MagicRenderClientApi.stopAllApiEffects()
                requestHandles.clear()
            }
        }
    }

    private fun anchor(payload: MagicRenderAnchorPayload): TrailAnchor? {
        return when (payload.type) {
            MagicRenderAnchorType.ENTITY -> {
                val world = Minecraft.getInstance().level ?: return null
                if (world.getEntity(payload.entityId) == null) return null
                TrailAnchor.Entity(payload.entityId, Vec3(payload.x, payload.y, payload.z))
            }
            MagicRenderAnchorType.WORLD_POINT -> TrailAnchor.WorldPoint(Vec3(payload.x, payload.y, payload.z))
        }
    }
}
