package io.github.yuazer.magicrender.client.editor

import io.github.yuazer.magicrender.client.api.MagicRenderClientApi
import io.github.yuazer.magicrender.client.effect.trajectory.TrailAnchor
import io.github.yuazer.magicrender.i18n.MagicRenderI18n.tr
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

object EffectEditorPreview {
    private val handles = mutableListOf<Long>()

    fun previewOnPlayer(draft: EffectEditorDraft): Component {
        stop()
        val validation = EffectEditorValidation.validate(draft)
        if (!validation.canExport) return tr("magicrender.editor.preview.blocked", validation.errors.first())

        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return tr("magicrender.editor.preview.no_world")
        val source = TrailAnchor.Entity(player.id, Vec3(0.0, player.getBbHeight().toDouble() * draft.preview.sourceHeightOffset, 0.0))
        val target = resolveTargetAnchor(draft)
        val effect = draft.toEffectDefinition()
        val handle = if (target != null) {
            MagicRenderClientApi.playEffect(effect, source, target)
        } else {
            MagicRenderClientApi.playEffect(effect, source)
        }
        if (handle != MagicRenderClientApi.NO_HANDLE) handles += handle

        return if (handles.isEmpty()) {
            tr("magicrender.editor.preview.none")
        } else {
            tr("magicrender.editor.preview.spawned", draft.preview.targetMode, handles.joinToString(","))
        }
    }

    fun stop() {
        for (handle in handles) {
            MagicRenderClientApi.stop(handle)
        }
        handles.clear()
    }

    private fun resolveTargetAnchor(draft: EffectEditorDraft): TrailAnchor? {
        if (!draft.components.beam.enabled) return null
        val minecraft = Minecraft.getInstance()
        val player = minecraft.player ?: return null
        return when (draft.preview.targetMode.lowercase()) {
            "crosshair_entity" -> {
                val entity = (minecraft.hitResult as? EntityHitResult)?.entity ?: minecraft.crosshairPickEntity
                if (entity != null) {
                    TrailAnchor.Entity(entity.id, Vec3(0.0, entity.getBbHeight().toDouble() * draft.preview.targetHeightOffset, 0.0))
                } else if (draft.preview.fallbackToFixedDistance) {
                    fixedDistanceAnchor(draft)
                } else {
                    null
                }
            }
            "look_point" -> {
                val hit = minecraft.hitResult
                when (hit?.type) {
                    HitResult.Type.ENTITY -> {
                        val entity = (hit as EntityHitResult).entity
                        TrailAnchor.Entity(entity.id, Vec3(0.0, entity.getBbHeight().toDouble() * draft.preview.targetHeightOffset, 0.0))
                    }
                    HitResult.Type.BLOCK -> TrailAnchor.WorldPoint((hit as BlockHitResult).location)
                    else -> fixedDistanceAnchor(draft)
                }
            }
            else -> fixedDistanceAnchor(draft)
        }
    }

    private fun fixedDistanceAnchor(draft: EffectEditorDraft): TrailAnchor? {
        val player = Minecraft.getInstance().player ?: return null
        val distance = draft.preview.fixedDistance.coerceIn(0.5, 64.0)
        val position = player.getEyePosition().add(player.getLookAngle().scale(distance))
        return TrailAnchor.WorldPoint(position)
    }
}
