package io.github.yuazer.magicrender.client.effect.trajectory

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

object MotionTrajectoryLayer {
    fun register() {
        ClientTickEvents.END_CLIENT_TICK.register { _ ->
            MotionEffectManager.tick()
            MagicCircleManager.tick()
        }
        WorldRenderEvents.AFTER_TRANSLUCENT.register { context ->
            val cameraPosition = context.camera().position
            MotionEffectManager.prepareFrame(cameraPosition)
            MagicCircleManager.prepareFrame(cameraPosition)
            RibbonRenderBackend.render(MotionEffectManager.lastFrameMeshes, cameraPosition)
            MagicCircleRenderBackend.render(MagicCircleManager.lastFrameMeshes, cameraPosition)
        }
    }
}
