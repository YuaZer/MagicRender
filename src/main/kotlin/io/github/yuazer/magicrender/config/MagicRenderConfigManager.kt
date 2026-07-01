package io.github.yuazer.magicrender.config

import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.writeText

object MagicRenderConfigManager {
    private val logger = LoggerFactory.getLogger("MagicRender/Config")
    private val configRoot: Path = FabricLoader.getInstance().configDir.resolve("magicrender")
    private val effectsRoot: Path = configRoot.resolve("effects")
    private val effectGroupsRoot: Path = configRoot.resolve("effects_group")

    @Volatile
    var current: LoadedMagicRenderConfig = LoadedMagicRenderConfig()
        private set

    fun initializeServer(): ConfigLoadResult {
        ensureDefaultFiles(includeClient = false)
        return reloadServer()
    }

    fun reloadServer(): ConfigLoadResult {
        val loaded = loadServerConfig()
        if (loaded.lastResult.success) {
            current = loaded
            logger.info("MagicRender server config reloaded: {}", loaded.lastResult.summary())
        } else {
            logger.warn("MagicRender server config reload failed; keeping previous config: {}", loaded.lastResult.summary())
        }
        return loaded.lastResult
    }

    fun validateServer(): ConfigLoadResult {
        return loadServerConfig().lastResult
    }

    fun ensureDefaultFiles(includeClient: Boolean) {
        configRoot.createDirectories()
        effectsRoot.createDirectories()
        effectGroupsRoot.createDirectories()
        writeDefaultIfMissing(configRoot.resolve("common.json"), DefaultConfigFiles.COMMON)
        writeDefaultIfMissing(configRoot.resolve("server.json"), DefaultConfigFiles.SERVER)
        writeDefaultIfMissing(effectsRoot.resolve("arcane_burst.json"), DefaultConfigFiles.ARCANE_BURST)
        writeDefaultIfMissing(effectsRoot.resolve("dash_trail.json"), DefaultConfigFiles.DASH_TRAIL)
        writeDefaultIfMissing(effectsRoot.resolve("mana_link.json"), DefaultConfigFiles.MANA_LINK)
        writeDefaultIfMissing(effectsRoot.resolve("entity_arcane_stream.json"), DefaultConfigFiles.ENTITY_ARCANE_STREAM)
        if (includeClient) {
            writeDefaultIfMissing(configRoot.resolve("client.json"), io.github.yuazer.magicrender.config.clientDefaultJson())
        }
    }

    private fun loadServerConfig(): LoadedMagicRenderConfig {
        val result = ConfigLoadAccumulator()
        ensureDefaultFiles(includeClient = false)

        val common = readJson(configRoot.resolve("common.json"), result)?.let {
            CommonConfig.parse(it, result, "common.json")
        } ?: CommonConfig()

        val server = readJson(configRoot.resolve("server.json"), result)?.let {
            ServerConfig.parse(it, result, "server.json")
        } ?: ServerConfig()

        val baseGroups = EffectGroupConfig()
        val groups = loadEffectGroupBindings(baseGroups, common, result)

        val fatalErrorsBeforeEffects = result.errors.size
        val effects = loadEffects(common, groups, result)
        val success = fatalErrorsBeforeEffects == 0
        return LoadedMagicRenderConfig(
            common = common,
            server = server,
            groups = groups,
            effects = effects,
            lastResult = result.toResult(success)
        )
    }

    private fun loadEffects(common: CommonConfig, groups: EffectGroupConfig, result: ConfigLoadAccumulator): Map<String, EffectDefinition> {
        if (!effectsRoot.exists()) return emptyMap()
        val effects = linkedMapOf<String, EffectDefinition>()
        Files.list(effectsRoot).use { stream ->
            stream
                .filter { it.isRegularFile() && it.name.endsWith(".json") }
                .sorted()
                .forEach { path ->
                    val json = readJson(path, result) ?: run {
                        result.skippedEffects += 1
                        return@forEach
                    }
                    val definition = EffectDefinition.parse(json, path.name, common, groups, result)
                    if (definition == null) {
                        result.skippedEffects += 1
                    } else {
                        effects[definition.id] = definition
                    }
                }
        }
        return effects
    }

    private fun loadEffectGroupBindings(base: EffectGroupConfig, common: CommonConfig, result: ConfigLoadAccumulator): EffectGroupConfig {
        if (!effectGroupsRoot.exists()) return base
        val groups = linkedMapOf<String, EffectGroup>()
        groups.putAll(base.groups)
        val bindings = linkedMapOf<String, List<String>>()
        bindings.putAll(base.bindings)
        Files.list(effectGroupsRoot).use { stream ->
            stream
                .filter { it.isRegularFile() && it.name.endsWith(".json") }
                .sorted()
                .forEach { path ->
                    val json = readJson(path, result) ?: return@forEach
                    val parsed = EffectGroupConfig.parse(json, common, result, "effects_group/${path.name}")
                    groups.putAll(parsed.groups)
                    bindings.putAll(parsed.bindings)
                }
        }
        return base.copy(groups = groups, bindings = bindings)
    }

    private fun readJson(path: Path, result: ConfigLoadAccumulator): com.google.gson.JsonObject? {
        if (!path.exists()) {
            result.warning(configRoot.relativize(path).toString(), "File is missing. Using defaults.")
            return null
        }
        return path.inputStream().bufferedReader(StandardCharsets.UTF_8).use { reader ->
            val parsed = parseJsonObject(reader, configRoot.relativize(path).toString().replace('\\', '/'), result)
            if (parsed != null) result.loadedFiles += 1
            parsed
        }
    }

    private fun writeDefaultIfMissing(path: Path, content: String) {
        if (!path.exists()) {
            path.parent.createDirectories()
            path.writeText(content, StandardCharsets.UTF_8)
        }
    }
}
