package io.github.yuazer.magicrender.client.editor.web

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.github.yuazer.magicrender.client.config.ClientConfigReloader
import io.github.yuazer.magicrender.client.editor.EffectEditorDraft
import io.github.yuazer.magicrender.client.editor.EffectEditorExporter
import io.github.yuazer.magicrender.client.editor.EffectEditorJson
import io.github.yuazer.magicrender.client.editor.EffectEditorPreview
import io.github.yuazer.magicrender.client.editor.EffectEditorProjectDraft
import io.github.yuazer.magicrender.client.editor.EffectEditorValidation
import io.github.yuazer.magicrender.config.obj
import io.github.yuazer.magicrender.config.MagicRenderConfigManager
import io.github.yuazer.magicrender.shadow.nanohttpd.NanoHTTPD
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.InetAddress
import java.nio.charset.StandardCharsets

class EffectEditorWebServer(
    private val bindHost: String,
    private val bindPort: Int
) : NanoHTTPD(bindHost, bindPort) {
    private val logger = LoggerFactory.getLogger("MagicRender/EditorWeb")
    private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    override fun serve(session: IHTTPSession): Response {
        return try {
            val uri = normalizeUri(session.uri)
            when {
                session.method == Method.GET && (uri == "/" || uri == "/index.html") -> resource("/assets/magicrender/editor/index.html", "text/html; charset=utf-8")
                session.method == Method.GET && uri == "/app.css" -> resource("/assets/magicrender/editor/app.css", "text/css; charset=utf-8")
                session.method == Method.GET && uri == "/app.js" -> resource("/assets/magicrender/editor/app.js", "application/javascript; charset=utf-8")
                session.method == Method.GET && uri == "/vendor/three.module.js" -> resource("/assets/magicrender/editor/vendor/three.module.js", "application/javascript; charset=utf-8")
                session.method == Method.GET && uri == "/vendor/OrbitControls.js" -> resource("/assets/magicrender/editor/vendor/OrbitControls.js", "application/javascript; charset=utf-8")
                session.method == Method.GET && uri == "/api/status" -> json(statusJson())
                session.method == Method.GET && uri == "/api/draft/default" -> json(EffectEditorDraftCodec.toEditorJson(EffectEditorDraft.entityArcaneStream()))
                session.method == Method.GET && uri == "/api/effects" -> json(effectsJson())
                session.method == Method.POST && uri == "/api/validate" -> json(validateJson(readJson(session)))
                session.method == Method.POST && uri == "/api/preview" -> json(previewJson(readJson(session)))
                session.method == Method.POST && uri == "/api/preview/stop" -> {
                    minecraftCall { EffectEditorPreview.stop() }
                    json(messageJson(true, "Preview stopped."))
                }
                session.method == Method.POST && uri == "/api/export" -> json(exportJson(readJson(session), overwrite = false))
                session.method == Method.POST && uri == "/api/export/overwrite" -> json(exportJson(readJson(session), overwrite = true))
                session.method == Method.POST && uri == "/api/export/group" -> json(exportGroupJson(readJson(session), overwrite = false))
                session.method == Method.POST && uri == "/api/export/group/overwrite" -> json(exportGroupJson(readJson(session), overwrite = true))
                session.method == Method.POST && uri == "/api/reload" -> {
                    val result = ClientConfigReloader.reloadClient()
                    json(messageJson(result.success, result.summary()))
                }
                else -> text(Response.Status.NOT_FOUND, "Not found")
            }.also { response ->
                response.addHeader("Access-Control-Allow-Origin", "http://127.0.0.1:$bindPort")
                response.addHeader("Cache-Control", "no-store")
            }
        } catch (exception: Exception) {
            logger.warn("Editor web request failed: {} {}", session.method, session.uri, exception)
            json(messageJson(false, exception.message ?: exception.javaClass.simpleName), Response.Status.INTERNAL_ERROR)
        }
    }

    fun startServer() {
        start(SOCKET_READ_TIMEOUT, false)
        logger.info("MagicRender editor web server started at http://{}:{}/", bindHost, bindPort)
    }

    private fun statusJson(): JsonObject {
        val config = ClientConfigReloader.current
        val json = JsonObject()
        json.addProperty("ok", true)
        json.addProperty("host", bindHost)
        json.addProperty("port", bindPort)
        json.addProperty("enabled", config.editor.enabled)
        json.addProperty("effects", MagicRenderConfigManager.current.effects.size)
        json.addProperty("url", "http://${bindHost}:${bindPort}/")
        return json
    }

    private fun effectsJson(): JsonObject {
        val root = JsonObject()
        val effects = com.google.gson.JsonArray()
        for (effect in MagicRenderConfigManager.current.effects.values) {
            val item = JsonObject()
            item.addProperty("id", effect.id)
            item.addProperty("group", effect.group)
            item.addProperty("enabled", effect.enabled)
            item.addProperty("durationTicks", effect.durationTicks)
            item.addProperty("trail", effect.components.trail.enabled)
            item.addProperty("beam", effect.components.beam.enabled)
            item.addProperty("magicCircle", effect.components.magicCircle.enabled)
            item.add("config", EffectEditorJson.toJsonObject(effect))
            effects.add(item)
        }
        root.add("effects", effects)
        return root
    }

    private fun validateJson(body: JsonObject): JsonObject {
        val draft = EffectEditorDraftCodec.fromJson(body)
        val validation = EffectEditorValidation.validate(draft)
        val json = JsonObject()
        json.addProperty("ok", validation.canExport)
        json.addProperty("summary", validation.summary)
        json.add("errors", validation.errors.map { it.string }.toJsonArray())
        json.add("warnings", validation.warnings.map { it.string }.toJsonArray())
        return json
    }

    private fun previewJson(body: JsonObject): JsonObject {
        val draft = EffectEditorDraftCodec.fromJson(body)
        val result = minecraftCall { EffectEditorPreview.previewOnPlayer(draft).string }
        return messageJson(true, result)
    }

    private fun exportJson(body: JsonObject, overwrite: Boolean): JsonObject {
        val draft = EffectEditorDraftCodec.fromJson(body)
        val result = EffectEditorExporter.export(draft, overwrite)
        val json = messageJson(result.success, result.message.string)
        result.path?.let { json.addProperty("path", it.toString()) }
        return json
    }

    private fun exportGroupJson(body: JsonObject, overwrite: Boolean): JsonObject {
        val project = projectFromJson(body)
        val includeEffects = body.get("includeEffects")?.let { runCatching { it.asBoolean }.getOrNull() } ?: true
        val result = EffectEditorExporter.exportGroup(project, overwrite, includeEffects)
        val json = messageJson(result.success, result.message.string)
        result.path?.let { json.addProperty("path", it.toString()) }
        return json
    }

    private fun projectFromJson(body: JsonObject): EffectEditorProjectDraft {
        val group = body.obj("group") ?: JsonObject()
        val effects = body.get("effects")?.takeIf { it.isJsonArray }?.asJsonArray ?: JsonArray()
        return EffectEditorProjectDraft(
            groupKey = group.get("key")?.asString ?: body.get("groupKey")?.asString ?: "magicrender:editor_group",
            description = group.get("description")?.asString ?: "",
            effects = effects.mapNotNull { element ->
                if (element.isJsonObject) EffectEditorDraftCodec.fromJson(element.asJsonObject) else null
            }
        )
    }

    private fun readJson(session: IHTTPSession): JsonObject {
        val files = HashMap<String, String>()
        session.parseBody(files)
        val body = files["postData"].orEmpty()
        if (body.isBlank()) return JsonObject()
        val element = JsonParser.parseString(body)
        return if (element.isJsonObject) element.asJsonObject else JsonObject()
    }

    private fun resource(path: String, mime: String): Response {
        val stream: InputStream = javaClass.getResourceAsStream(path) ?: return text(Response.Status.NOT_FOUND, "Missing resource: $path")
        val content = stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        return newFixedLengthResponse(Response.Status.OK, mime, content)
    }

    private fun json(json: JsonObject, status: Response.Status = Response.Status.OK): Response {
        return newFixedLengthResponse(status, "application/json; charset=utf-8", gson.toJson(json))
    }

    private fun text(status: Response.Status, text: String): Response {
        return newFixedLengthResponse(status, "text/plain; charset=utf-8", text)
    }

    private fun messageJson(ok: Boolean, message: String): JsonObject {
        val json = JsonObject()
        json.addProperty("ok", ok)
        json.addProperty("message", message)
        return json
    }

    private fun normalizeUri(uri: String): String {
        val clean = uri.substringBefore('?')
        return clean.ifBlank { "/" }
    }

    private fun <T> minecraftCall(action: () -> T): T {
        val minecraft = Minecraft.getInstance()
        val future = java.util.concurrent.CompletableFuture<T>()
        minecraft.execute {
            try {
                future.complete(action())
            } catch (exception: Exception) {
                future.completeExceptionally(exception)
            }
        }
        return future.get()
    }

    private fun List<String>.toJsonArray(): com.google.gson.JsonArray {
        val array = com.google.gson.JsonArray()
        forEach(array::add)
        return array
    }
}

object EffectEditorWebServerManager {
    private val logger = LoggerFactory.getLogger("MagicRender/EditorWeb")

    @Volatile
    private var server: EffectEditorWebServer? = null

    fun startFromConfig() {
        val config = ClientConfigReloader.current.editor
        stop()
        if (!config.enabled) {
            logger.info("MagicRender editor web server is disabled in client config.")
            return
        }
        val bindAddress = InetAddress.getByName(config.host)
        if (!bindAddress.isLoopbackAddress) {
            logger.warn("MagicRender editor host `{}` is not loopback. Use 127.0.0.1 unless you intentionally expose the editor.", config.host)
        }
        val next = EffectEditorWebServer(config.host, config.port)
        next.startServer()
        server = next
    }

    fun stop() {
        server?.stop()
        server = null
    }
}
