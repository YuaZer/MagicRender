package io.github.yuazer.magicrender.network

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3

object MagicRenderPayloads {
    fun registerCommon() {
        PayloadTypeRegistry.playS2C().register(PlayEffectPayload.TYPE, PlayEffectPayload.CODEC)
        PayloadTypeRegistry.playS2C().register(StopEffectPayload.TYPE, StopEffectPayload.CODEC)
    }
}

data class PlayEffectPayload(
    val requestId: Long,
    val effectId: String,
    val mode: MagicRenderPlayMode,
    val source: MagicRenderAnchorPayload,
    val target: MagicRenderAnchorPayload?
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<PlayEffectPayload> = CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("magicrender", "play_effect"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, PlayEffectPayload> = StreamCodec.of(::encode, ::decode)

        private fun encode(buf: RegistryFriendlyByteBuf, payload: PlayEffectPayload) {
            buf.writeLong(payload.requestId)
            buf.writeUtf(payload.effectId)
            buf.writeVarInt(payload.mode.ordinal)
            MagicRenderAnchorPayload.encode(buf, payload.source)
            buf.writeBoolean(payload.target != null)
            payload.target?.let { MagicRenderAnchorPayload.encode(buf, it) }
        }

        private fun decode(buf: RegistryFriendlyByteBuf): PlayEffectPayload {
            val requestId = buf.readLong()
            val effectId = buf.readUtf()
            val mode = MagicRenderPlayMode.entries.getOrElse(buf.readVarInt()) { MagicRenderPlayMode.EFFECT }
            val source = MagicRenderAnchorPayload.decode(buf)
            val target = if (buf.readBoolean()) MagicRenderAnchorPayload.decode(buf) else null
            return PlayEffectPayload(requestId, effectId, mode, source, target)
        }
    }
}

data class StopEffectPayload(
    val requestId: Long,
    val stopMode: MagicRenderStopMode,
    val effectId: String,
    val entityId: Int
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = TYPE

    companion object {
        val TYPE: CustomPacketPayload.Type<StopEffectPayload> = CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("magicrender", "stop_effect"))
        val CODEC: StreamCodec<RegistryFriendlyByteBuf, StopEffectPayload> = StreamCodec.of(::encode, ::decode)

        private fun encode(buf: RegistryFriendlyByteBuf, payload: StopEffectPayload) {
            buf.writeLong(payload.requestId)
            buf.writeVarInt(payload.stopMode.ordinal)
            buf.writeUtf(payload.effectId)
            buf.writeVarInt(payload.entityId)
        }

        private fun decode(buf: RegistryFriendlyByteBuf): StopEffectPayload {
            return StopEffectPayload(
                requestId = buf.readLong(),
                stopMode = MagicRenderStopMode.entries.getOrElse(buf.readVarInt()) { MagicRenderStopMode.REQUEST },
                effectId = buf.readUtf(),
                entityId = buf.readVarInt()
            )
        }
    }
}

data class MagicRenderAnchorPayload(
    val type: MagicRenderAnchorType,
    val entityId: Int,
    val x: Double,
    val y: Double,
    val z: Double
) {
    companion object {
        fun entity(entityId: Int, offset: Vec3 = Vec3.ZERO): MagicRenderAnchorPayload {
            return MagicRenderAnchorPayload(MagicRenderAnchorType.ENTITY, entityId, offset.x, offset.y, offset.z)
        }

        fun world(position: Vec3): MagicRenderAnchorPayload {
            return MagicRenderAnchorPayload(MagicRenderAnchorType.WORLD_POINT, 0, position.x, position.y, position.z)
        }

        fun encode(buf: RegistryFriendlyByteBuf, anchor: MagicRenderAnchorPayload) {
            buf.writeVarInt(anchor.type.ordinal)
            buf.writeVarInt(anchor.entityId)
            buf.writeDouble(anchor.x)
            buf.writeDouble(anchor.y)
            buf.writeDouble(anchor.z)
        }

        fun decode(buf: RegistryFriendlyByteBuf): MagicRenderAnchorPayload {
            return MagicRenderAnchorPayload(
                type = MagicRenderAnchorType.entries.getOrElse(buf.readVarInt()) { MagicRenderAnchorType.WORLD_POINT },
                entityId = buf.readVarInt(),
                x = buf.readDouble(),
                y = buf.readDouble(),
                z = buf.readDouble()
            )
        }
    }
}

enum class MagicRenderPlayMode {
    EFFECT,
    TRAIL,
    MAGIC_CIRCLE,
    BEAM,
    STREAM,
    GROUP
}

enum class MagicRenderStopMode {
    REQUEST,
    EFFECT_ID,
    GROUP_KEY,
    ENTITY_ID,
    ALL
}

enum class MagicRenderAnchorType {
    ENTITY,
    WORLD_POINT
}
