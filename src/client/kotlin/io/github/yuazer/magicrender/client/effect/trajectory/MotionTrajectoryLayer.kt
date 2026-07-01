package io.github.yuazer.magicrender.client.effect.trajectory

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object MotionTrajectoryLayer {
    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            MotionEffectManager.tick()
            MagicCircleManager.tick()
            AdvancedEffectManager.tick()
        }
        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            val cameraPosition = context.camera().position
            val world = net.minecraft.client.Minecraft.getInstance().level
            val tickDelta = context.tickCounter().getGameTimeDeltaPartialTick(false)
            val renderContext = TrajectoryRenderContext(
                cameraPosition = cameraPosition,
                tickDelta = tickDelta,
                renderTimeTicks = (world?.gameTime ?: 0L).toDouble() + tickDelta.toDouble(),
                nowNanos = System.nanoTime()
            )
            MotionEffectManager.prepareFrame(renderContext)
            MagicCircleManager.prepareFrame(renderContext)
            AdvancedEffectManager.prepareFrame(renderContext)
            RibbonRenderBackend.render(MotionEffectManager.lastFrameMeshes, cameraPosition)
            RibbonRenderBackend.render(AdvancedEffectManager.lastFrameRibbons, cameraPosition)
            MagicCircleRenderBackend.render(MagicCircleManager.lastFrameMeshes, cameraPosition)
            MagicCircleRenderBackend.render(AdvancedEffectManager.lastFrameCircles, cameraPosition)
            BillboardRenderBackend.render(AdvancedEffectManager.lastFrameBillboards, cameraPosition)
            GlowPostProcessor.render(
                glow = AdvancedEffectManager.lastFrameGlow,
                cameraPosition = cameraPosition,
                ribbons = AdvancedEffectManager.lastFrameRibbons,
                circles = AdvancedEffectManager.lastFrameCircles,
                billboards = AdvancedEffectManager.lastFrameBillboards
            )
        }
    }
}
