package io.github.yuazer.magicrender.client.editor

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import io.github.yuazer.magicrender.i18n.MagicRenderI18n.tr
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.chat.Component
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

data class EffectEditorExportResult(
    val success: Boolean,
    val path: Path?,
    val message: Component
)

object EffectEditorExporter {
    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
    private val configRoot: Path
        get() = FabricLoader.getInstance().configDir.resolve("magicrender")

    fun export(draft: EffectEditorDraft, overwrite: Boolean): EffectEditorExportResult {
        val validation = EffectEditorValidation.validate(draft)
        if (!validation.canExport) {
            return EffectEditorExportResult(false, null, tr("magicrender.editor.export.invalid", validation.errors.first()))
        }

        val root = configRoot.resolve("effects").normalize()
        val target = root.resolve(fileNameFor(draft.id)).normalize()
        return writeJson(target, root, draft.toJsonObject(), overwrite, draft.id)
    }

    fun exportGroup(project: EffectEditorProjectDraft, overwrite: Boolean, includeEffects: Boolean): EffectEditorExportResult {
        if (!isValidProjectId(project.groupKey)) {
            return EffectEditorExportResult(false, null, tr("magicrender.editor.export.invalid", "groupKey"))
        }
        if (project.effects.isEmpty()) {
            return EffectEditorExportResult(false, null, tr("magicrender.editor.export.invalid", "empty group"))
        }

        val effectsRoot = configRoot.resolve("effects").normalize()
        val groupRoot = configRoot.resolve("effects_group").normalize()
        val groupTarget = groupRoot.resolve(fileNameFor(project.groupKey)).normalize()

        return try {
            if (includeEffects) {
                for (draft in project.effects) {
                    val validation = EffectEditorValidation.validate(draft)
                    if (!validation.canExport) {
                        return EffectEditorExportResult(false, null, tr("magicrender.editor.export.invalid", validation.errors.first()))
                    }
                    val effectTarget = effectsRoot.resolve(fileNameFor(draft.id)).normalize()
                    val effectResult = writeJson(effectTarget, effectsRoot, draft.toJsonObject(), overwrite, draft.id)
                    if (!effectResult.success) return effectResult
                }
            }

            writeJson(groupTarget, groupRoot, project.toGroupJson(), overwrite, project.groupKey)
        } catch (exception: Exception) {
            EffectEditorExportResult(false, groupTarget, tr("magicrender.editor.export.failed", exception.message ?: exception.javaClass.simpleName))
        }
    }

    private fun writeJson(target: Path, root: Path, json: JsonObject, overwrite: Boolean, label: String): EffectEditorExportResult {
        if (!target.startsWith(root)) {
            return EffectEditorExportResult(false, null, tr("magicrender.editor.export.outside_root"))
        }
        if (Files.exists(target) && !overwrite) {
            return EffectEditorExportResult(false, target, tr("magicrender.editor.export.exists"))
        }

        return try {
            Files.createDirectories(root)
            val temp = Files.createTempFile(root, target.fileName.toString(), ".tmp")
            Files.writeString(
                temp,
                gson.toJson(json),
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
            }
            EffectEditorExportResult(true, target, tr("magicrender.editor.export.success", label))
        } catch (exception: Exception) {
            EffectEditorExportResult(false, target, tr("magicrender.editor.export.failed", exception.message ?: exception.javaClass.simpleName))
        }
    }

    private fun fileNameFor(id: String): String {
        val parts = id.split(":", limit = 2)
        val namespace = parts.getOrElse(0) { "magicrender" }
        val path = parts.getOrElse(1) { id }.replace('/', '_')
        return if (namespace == "magicrender") "$path.json" else "${namespace}__$path.json"
    }

    private fun isValidProjectId(value: String): Boolean {
        return value.matches(Regex("[a-z0-9_.-]+:[a-z0-9_./-]+"))
    }
}

data class EffectEditorProjectDraft(
    val groupKey: String,
    val description: String = "",
    val effects: List<EffectEditorDraft>
) {
    fun toGroupJson(): JsonObject {
        val root = JsonObject()
        root.addProperty("version", 1)
        val groups = JsonObject()
        val item = JsonObject()
        item.addProperty("enabled", true)
        item.addProperty("description", description)
        val effectsArray = JsonArray()
        for (effectId in effects.map { it.id }.distinct()) {
            effectsArray.add(effectId)
        }
        item.add("effects", effectsArray)
        groups.add(groupKey, item)
        root.add("groups", groups)
        return root
    }
}
