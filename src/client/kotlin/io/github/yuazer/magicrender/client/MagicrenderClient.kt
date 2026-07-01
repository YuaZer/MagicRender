package io.github.yuazer.magicrender.client

import io.github.yuazer.magicrender.client.command.MagicRenderClientCommands
import io.github.yuazer.magicrender.client.config.ClientConfigReloader
import io.github.yuazer.magicrender.client.editor.web.EffectEditorWebServerManager
import io.github.yuazer.magicrender.client.effect.trajectory.GlowPostProcessor
import io.github.yuazer.magicrender.client.effect.trajectory.MotionTrajectoryLayer
import io.github.yuazer.magicrender.client.network.MagicRenderClientNetwork
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import org.slf4j.LoggerFactory

class MagicrenderClient : ClientModInitializer {
    private val logger = LoggerFactory.getLogger("MagicRender/Client")

    override fun onInitializeClient() {
        MagicRenderConfigManager.ensureDefaultFiles(includeClient = true)
        val result = ClientConfigReloader.initialize()
        logger.info("MagicRender client initialized: {}", result.summary())
        MagicRenderClientCommands.register()
        MagicRenderClientNetwork.register()
        MotionTrajectoryLayer.register()
        EffectEditorWebServerManager.startFromConfig()
        ClientLifecycleEvents.CLIENT_STOPPING.register {
            GlowPostProcessor.close()
            EffectEditorWebServerManager.stop()
        }
    }
}
