package io.github.yuazer.magicrender.command

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import io.github.yuazer.magicrender.api.MagicRenderServerApi
import io.github.yuazer.magicrender.config.ConfigLoadResult
import io.github.yuazer.magicrender.config.ConfigMessageLevel
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.i18n.MagicRenderI18n.tr
import io.github.yuazer.magicrender.network.MagicRenderPlayMode
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.SharedSuggestionProvider

object MagicRenderCommands {
    fun register() {
        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            dispatcher.register(root())
        }
    }

    private fun root(): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal("magicrender")
            .then(
                Commands.literal("reload")
                    .requires { it.hasPermission(MagicRenderConfigManager.current.server.permissions.reloadRequiresLevel) }
                    .executes { context ->
                        val result = MagicRenderConfigManager.reloadServer()
                        sendResult(context.source, "Reload", result)
                        Command.SINGLE_SUCCESS
                    }
                    .then(
                        Commands.literal("server")
                            .executes { context ->
                                val result = MagicRenderConfigManager.reloadServer()
                                sendResult(context.source, "Server reload", result)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .then(
                Commands.literal("bind")
                    .executes { context ->
                        context.source.sendSuccess(
                            {
                                tr("magicrender.command.bind.client_side")
                            },
                            false
                        )
                        Command.SINGLE_SUCCESS
                    }
            )
            .then(
                Commands.literal("play")
                    .requires { it.hasPermission(MagicRenderConfigManager.current.server.permissions.spawnTestEffectRequiresLevel) }
                    .then(
                        Commands.literal("self")
                            .then(effectArgument { source, effectId ->
                                val player = source.playerOrException
                                val requestId = MagicRenderServerApi.play(player, effectId, player, MagicRenderPlayMode.EFFECT)
                                source.sendSuccess({ tr("magicrender.command.play.sent", effectId, requestId) }, false)
                                Command.SINGLE_SUCCESS
                            })
                    )
                    .then(
                        Commands.literal("nearby")
                            .then(effectArgument { source, effectId ->
                                val player = source.playerOrException
                                val target = player.level().getEntities(player, player.boundingBox.inflate(64.0))
                                    .minByOrNull { it.distanceToSqr(player) }
                                    ?: return@effectArgument 0
                                val requestId = MagicRenderServerApi.play(player, effectId, target, MagicRenderPlayMode.EFFECT)
                                source.sendSuccess({ tr("magicrender.command.play.sent", effectId, requestId) }, false)
                                Command.SINGLE_SUCCESS
                            })
                    )
            )
            .then(
                Commands.literal("stop")
                    .requires { it.hasPermission(MagicRenderConfigManager.current.server.permissions.spawnTestEffectRequiresLevel) }
                    .then(
                        Commands.literal("all")
                            .executes { context ->
                                MagicRenderServerApi.stopAll(context.source.playerOrException)
                                context.source.sendSuccess({ tr("magicrender.command.stop.sent") }, false)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
            .then(
                Commands.literal("config")
                    .then(
                        Commands.literal("status")
                            .executes { context ->
                                sendStatus(context.source)
                                Command.SINGLE_SUCCESS
                            }
                    )
                    .then(
                        Commands.literal("validate")
                            .requires { it.hasPermission(MagicRenderConfigManager.current.server.permissions.reloadRequiresLevel) }
                            .executes { context ->
                                val result = MagicRenderConfigManager.validateServer()
                                sendResult(context.source, "Validate", result)
                                Command.SINGLE_SUCCESS
                            }
                    )
            )
    }

    private fun effectArgument(action: (CommandSourceStack, String) -> Int): RequiredArgumentBuilder<CommandSourceStack, String> {
        return Commands.argument("effect", StringArgumentType.greedyString())
            .suggests { _, builder ->
                SharedSuggestionProvider.suggest(MagicRenderConfigManager.current.effects.keys, builder)
            }
            .executes { context ->
                action(context.source, StringArgumentType.getString(context, "effect"))
            }
    }

    private fun sendStatus(source: CommandSourceStack) {
        val config = MagicRenderConfigManager.current
        val result = config.lastResult
        source.sendSuccess(
            {
                tr(
                    "magicrender.command.config.status",
                    config.common.enabled && config.server.enabled,
                    config.effects.size,
                    config.groups.groups.size,
                    result.summary()
                )
            },
            false
        )
        sendImportantMessages(source, result)
    }

    private fun sendResult(source: CommandSourceStack, action: String, result: ConfigLoadResult) {
        val actionKey = when (action) {
            "Server reload" -> "magicrender.action.server_reload"
            "Validate" -> "magicrender.action.validate"
            else -> "magicrender.action.reload"
        }
        val resultKey = if (result.success) "magicrender.result.succeeded" else "magicrender.result.failed_keep_previous"
        source.sendSuccess(
            { tr("magicrender.command.result", tr(actionKey), tr(resultKey), result.summary()) },
            false
        )
        sendImportantMessages(source, result)
    }

    private fun sendImportantMessages(source: CommandSourceStack, result: ConfigLoadResult) {
        result.messages
            .filter { it.level == ConfigMessageLevel.ERROR || it.level == ConfigMessageLevel.WARNING }
            .takeLast(8)
            .forEach { message ->
                source.sendSuccess(
                    { tr("magicrender.command.config.message", message.level.name, message.path, message.message) },
                    false
                )
            }
    }
}
