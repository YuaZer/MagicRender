package io.github.yuazer.magicrender.client.config

import io.github.yuazer.magicrender.client.compat.RenderCompat
import io.github.yuazer.magicrender.client.compat.RenderCompatibilityState
import io.github.yuazer.magicrender.config.ConfigLoadAccumulator
import io.github.yuazer.magicrender.config.ConfigLoadResult
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.config.parseJsonObject
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream

object ClientConfigReloader {
    private val logger = LoggerFactory.getLogger("MagicRender/ClientConfig")
    private val configRoot: Path = FabricLoader.getInstance().configDir.resolve("magicrender")
    private val clientConfigPath: Path = configRoot.resolve("client.json")

    @Volatile
    var current: ClientConfig = ClientConfig()
        private set

    @Volatile
    var compatibility: RenderCompatibilityState = RenderCompat.evaluate(current)
        private set

    @Volatile
    var lastResult: ConfigLoadResult = ConfigLoadResult.EMPTY
        private set

    fun initialize(): ConfigLoadResult {
        MagicRenderConfigManager.ensureDefaultFiles(includeClient = true)
        return reloadClient()
    }

    fun reloadClient(): ConfigLoadResult {
        MagicRenderConfigManager.ensureDefaultFiles(includeClient = true)
        val sharedResult = MagicRenderConfigManager.reloadServer()
        val accumulator = ConfigLoadAccumulator()
        val parsedClient = readClient(accumulator) ?: current
        val success = accumulator.errors.isEmpty()
        val clientResult = accumulator.toResult(success)

        if (success) {
            current = parsedClient
            compatibility = RenderCompat.evaluate(parsedClient)
            lastResult = mergeResults(sharedResult, clientResult, success = sharedResult.success)
            if (current.debug.logReloads) {
                logger.info("MagicRender client config reloaded: {}", lastResult.summary())
            }
        } else {
            lastResult = mergeResults(sharedResult, clientResult, success = false)
            logger.warn("MagicRender client config reload failed; keeping previous client config: {}", lastResult.summary())
        }

        return lastResult
    }

    fun validateClient(): ConfigLoadResult {
        val sharedResult = MagicRenderConfigManager.validateServer()
        val accumulator = ConfigLoadAccumulator()
        readClient(accumulator)
        return mergeResults(sharedResult, accumulator.toResult(accumulator.errors.isEmpty()), success = sharedResult.success && accumulator.errors.isEmpty())
    }

    private fun readClient(result: ConfigLoadAccumulator): ClientConfig? {
        if (!clientConfigPath.exists()) {
            result.warning("client.json", "File is missing. Using defaults.")
            return ClientConfig()
        }
        return clientConfigPath.inputStream().bufferedReader(StandardCharsets.UTF_8).use { reader ->
            val json = parseJsonObject(reader, "client.json", result) ?: return@use null
            result.loadedFiles += 1
            ClientConfig.parse(json, result, "client.json")
        }
    }

    private fun mergeResults(shared: ConfigLoadResult, client: ConfigLoadResult, success: Boolean): ConfigLoadResult {
        return ConfigLoadResult(
            success = success && shared.success && client.success,
            loadedFiles = shared.loadedFiles + client.loadedFiles,
            skippedEffects = shared.skippedEffects + client.skippedEffects,
            messages = shared.messages + client.messages
        )
    }
}

