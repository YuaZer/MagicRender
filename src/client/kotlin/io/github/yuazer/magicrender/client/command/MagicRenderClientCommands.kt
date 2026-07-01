package io.github.yuazer.magicrender.client.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import io.github.yuazer.magicrender.client.api.MagicRenderClientApi
import io.github.yuazer.magicrender.client.config.ClientConfigReloader
import io.github.yuazer.magicrender.client.editor.web.EffectEditorWebServerManager
import io.github.yuazer.magicrender.config.ConfigLoadResult
import io.github.yuazer.magicrender.config.ConfigMessageLevel
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.config.isValidIdentifier
import io.github.yuazer.magicrender.i18n.MagicRenderI18n.tr
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.Minecraft
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.EntityHitResult

object MagicRenderClientCommands {
    fun register() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(root("magicrender"))
            dispatcher.register(root("mrender"))
            dispatcher.register(root("magicrenderclient"))
        }
    }

    private fun root(name: String): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal(name)
            .then(
                ClientCommandManager.literal("reload")
                    .executes { context ->
                        val result = ClientConfigReloader.reloadClient()
                        EffectEditorWebServerManager.startFromConfig()
                        sendResult(context.source, "Client reload", result)
                        Command.SINGLE_SUCCESS
                    }
                    .then(
                        ClientCommandManager.literal("client")
                            .executes { context ->
                                val result = ClientConfigReloader.reloadClient()
                                EffectEditorWebServerManager.startFromConfig()
                                sendResult(context.source, "Client reload", result)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .then(
                ClientCommandManager.literal("bind")
                    .then(bindType("circle"))
                    .then(bindType("trail"))
                    .then(bindType("stream"))
                    .then(bindGroup())
            )
            .then(bindGroupAlias())
            .then(
                ClientCommandManager.literal("config")
                    .then(
                        ClientCommandManager.literal("status")
                            .executes { context ->
                                sendStatus(context.source)
                                Command.SINGLE_SUCCESS
                            }
                    )
                    .then(
                        ClientCommandManager.literal("validate")
                            .executes { context ->
                                val result = ClientConfigReloader.validateClient()
                                sendResult(context.source, "Client validate", result)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
    }

    private fun bindType(type: String): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal(type)
            .then(
                ClientCommandManager.argument("effect", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        SharedSuggestionProvider.suggest(MagicRenderConfigManager.current.effects.keys, builder)
                    }
                    .executes { context ->
                        val effectId = StringArgumentType.getString(context, "effect")
                        bindToCrosshairEntity(context.source, type, effectId)
                    }
            )
    }

    private fun bindGroup(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal("group")
            .then(
                ClientCommandManager.argument("group", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        SharedSuggestionProvider.suggest(MagicRenderClientApi.loadedGroupKeys(), builder)
                    }
                    .executes { context ->
                        val groupKey = StringArgumentType.getString(context, "group")
                        bindGroupToCrosshairEntity(context.source, groupKey)
                    }
            )
    }

    private fun bindGroupAlias(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return ClientCommandManager.literal("bindGroup")
            .then(
                ClientCommandManager.argument("group", StringArgumentType.greedyString())
                    .suggests { _, builder ->
                        SharedSuggestionProvider.suggest(MagicRenderClientApi.loadedGroupKeys(), builder)
                    }
                    .executes { context ->
                        val groupKey = StringArgumentType.getString(context, "group")
                        bindGroupToCrosshairEntity(context.source, groupKey)
                    }
            )
    }

    private fun bindToCrosshairEntity(source: FabricClientCommandSource, type: String, effectId: String): Int {
        if (!isValidIdentifier(effectId)) {
            source.sendError(tr("magicrender.command.bind.invalid_id", effectId))
            return 0
        }

        val effect = MagicRenderConfigManager.current.effects[effectId]
        if (effect == null) {
            source.sendError(tr("magicrender.command.bind.effect_not_loaded", effectId))
            return 0
        }

        val target = crosshairEntity()
        if (target == null) {
            source.sendError(tr("magicrender.command.bind.no_entity"))
            return 0
        }

        val handles = when (type) {
            "circle" -> listOf(MagicRenderClientApi.bindEntityMagicCircle(effectId, target))
            "trail" -> listOf(MagicRenderClientApi.bindEntityTrail(effectId, target))
            "stream" -> listOf(MagicRenderClientApi.bindEntityStream(effectId, target))
            else -> emptyList()
        }.filter { it != MagicRenderClientApi.NO_HANDLE }

        if (handles.isEmpty()) {
            source.sendError(tr("magicrender.command.bind.spawn_failed", effectId, type))
            return 0
        }

        source.sendFeedback(
            tr("magicrender.command.bind.success", type, effectId, target.name.string, target.id, handles.joinToString(","))
        )
        return Command.SINGLE_SUCCESS
    }

    private fun bindGroupToCrosshairEntity(source: FabricClientCommandSource, groupKey: String): Int {
        if (MagicRenderClientApi.loadedGroupEffectIds(groupKey).isEmpty()) {
            source.sendError(tr("magicrender.command.bind.group_not_loaded", groupKey))
            return 0
        }

        val target = crosshairEntity()
        if (target == null) {
            source.sendError(tr("magicrender.command.bind.no_entity"))
            return 0
        }

        val handle = MagicRenderClientApi.bindGroup(groupKey, target)
        if (handle == MagicRenderClientApi.NO_HANDLE) {
            source.sendError(tr("magicrender.command.bind.spawn_failed", groupKey, "group"))
            return 0
        }

        source.sendFeedback(
            tr("magicrender.command.bind_group.success", groupKey, target.name.string, target.id, handle)
        )
        return Command.SINGLE_SUCCESS
    }

    private fun crosshairEntity(): Entity? {
        val minecraft = Minecraft.getInstance()
        val hitResult = minecraft.hitResult
        if (hitResult is EntityHitResult) return hitResult.entity
        return minecraft.crosshairPickEntity
    }

    private fun sendStatus(source: FabricClientCommandSource) {
        val shared = MagicRenderConfigManager.current
        val client = ClientConfigReloader.current
        val compat = ClientConfigReloader.compatibility
        source.sendFeedback(
            tr(
                "magicrender.command.client_config.status",
                shared.effects.size,
                shared.groups.groups.size,
                client.compatibility.mode,
                compat.resolvedMode,
                compat.irisLoaded,
                compat.sodiumLoaded,
                compat.screenEffectsAllowed,
                compat.offscreenCompositionAllowed
            )
        )
        sendImportantMessages(source, ClientConfigReloader.lastResult)
    }

    private fun sendResult(source: FabricClientCommandSource, action: String, result: ConfigLoadResult) {
        val compat = ClientConfigReloader.compatibility
        val actionKey = when (action) {
            "Client reload" -> "magicrender.action.client_reload"
            else -> "magicrender.action.client_validate"
        }
        val resultKey = if (result.success) "magicrender.result.succeeded" else "magicrender.result.failed_keep_previous"
        source.sendFeedback(
            tr(
                "magicrender.command.client_result",
                tr(actionKey),
                tr(resultKey),
                result.summary(),
                compat.irisLoaded,
                compat.sodiumLoaded,
                compat.resolvedMode
            )
        )
        sendImportantMessages(source, result)
    }

    private fun sendImportantMessages(source: FabricClientCommandSource, result: ConfigLoadResult) {
        result.messages
            .filter { it.level == ConfigMessageLevel.ERROR || it.level == ConfigMessageLevel.WARNING }
            .takeLast(8)
            .forEach { message ->
                source.sendFeedback(tr("magicrender.command.config.message", message.level.name, message.path, message.message))
            }
    }
}
