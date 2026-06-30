package io.github.yuazer.magicrender.client.editor

import com.google.gson.GsonBuilder
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

    fun export(draft: EffectEditorDraft, overwrite: Boolean): EffectEditorExportResult {
        val validation = EffectEditorValidation.validate(draft)
        if (!validation.canExport) {
            return EffectEditorExportResult(false, null, tr("magicrender.editor.export.invalid", validation.errors.first()))
        }

        val root = FabricLoader.getInstance().configDir.resolve("magicrender").resolve("effects").normalize()
        val target = root.resolve(fileNameFor(draft.id)).normalize()
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
                gson.toJson(draft.toJsonObject()),
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
            )
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (_: Exception) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING)
            }
            EffectEditorExportResult(true, target, tr("magicrender.editor.export.success", draft.id))
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
}
