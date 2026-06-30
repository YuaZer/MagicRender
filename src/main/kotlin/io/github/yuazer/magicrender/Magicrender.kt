package io.github.yuazer.magicrender

import io.github.yuazer.magicrender.command.MagicRenderCommands
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.network.MagicRenderPayloads
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

class Magicrender : ModInitializer {
    private val logger = LoggerFactory.getLogger("MagicRender")

    override fun onInitialize() {
        val result = MagicRenderConfigManager.initializeServer()
        logger.info("MagicRender common initialized: {}", result.summary())
        MagicRenderPayloads.registerCommon()
        MagicRenderCommands.register()
    }
}
