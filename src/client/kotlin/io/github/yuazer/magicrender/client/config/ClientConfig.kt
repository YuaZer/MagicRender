package io.github.yuazer.magicrender.client.config

import com.google.gson.JsonObject
import io.github.yuazer.magicrender.config.ConfigLoadAccumulator
import io.github.yuazer.magicrender.config.boolean
import io.github.yuazer.magicrender.config.double
import io.github.yuazer.magicrender.config.int
import io.github.yuazer.magicrender.config.obj
import io.github.yuazer.magicrender.config.string
import io.github.yuazer.magicrender.config.warnUnknownFields

data class ClientConfig(
    val version: Int = 1,
    val quality: ClientQuality = ClientQuality(),
    val compatibility: ClientCompatibility = ClientCompatibility(),
    val visuals: ClientVisuals = ClientVisuals(),
    val debug: ClientDebug = ClientDebug(),
    val editor: ClientEditor = ClientEditor()
) {
    companion object {
        fun parse(json: JsonObject, result: ConfigLoadAccumulator, path: String): ClientConfig {
            val default = ClientConfig()
            json.warnUnknownFields(setOf("version", "quality", "compatibility", "visuals", "debug", "editor"), path, result)
            return ClientConfig(
                version = json.int("version", default.version, 1, 1, path, result),
                quality = ClientQuality.parse(json.obj("quality"), default.quality, "$path.quality", result),
                compatibility = ClientCompatibility.parse(json.obj("compatibility"), default.compatibility),
                visuals = ClientVisuals.parse(json.obj("visuals"), default.visuals),
                debug = ClientDebug.parse(json.obj("debug"), default.debug),
                editor = ClientEditor.parse(json.obj("editor"), default.editor, "$path.editor", result)
            )
        }
    }
}

data class ClientEditor(
    val enabled: Boolean = true,
    val host: String = "127.0.0.1",
    val port: Int = 3566
) {
    companion object {
        fun parse(json: JsonObject?, default: ClientEditor, path: String, result: ConfigLoadAccumulator): ClientEditor {
            if (json == null) return default
            return ClientEditor(
                enabled = json.boolean("enabled", default.enabled),
                host = json.string("host", default.host).ifBlank { default.host },
                port = json.int("port", default.port, 1024, 65535, path, result)
            )
        }
    }
}

data class ClientQuality(
    val preset: String = "balanced",
    val particleMultiplier: Double = 1.0,
    val effectDistanceMultiplier: Double = 1.0
) {
    companion object {
        fun parse(json: JsonObject?, default: ClientQuality, path: String, result: ConfigLoadAccumulator): ClientQuality {
            if (json == null) return default
            return ClientQuality(
                preset = json.string("preset", default.preset),
                particleMultiplier = json.double("particleMultiplier", default.particleMultiplier, 0.0, 2.0, path, result),
                effectDistanceMultiplier = json.double("effectDistanceMultiplier", default.effectDistanceMultiplier, 0.0, 2.0, path, result)
            )
        }
    }
}

data class ClientCompatibility(
    val mode: String = "auto",
    val whenIrisLoaded: String = "normal",
    val whenSodiumLoaded: String = "normal",
    val disableScreenEffectsWithShaders: Boolean = false,
    val disableOffscreenCompositionWithShaders: Boolean = false
) {
    companion object {
        fun parse(json: JsonObject?, default: ClientCompatibility): ClientCompatibility {
            if (json == null) return default
            return ClientCompatibility(
                mode = json.string("mode", default.mode),
                whenIrisLoaded = json.string("whenIrisLoaded", default.whenIrisLoaded),
                whenSodiumLoaded = json.string("whenSodiumLoaded", default.whenSodiumLoaded),
                disableScreenEffectsWithShaders = json.boolean("disableScreenEffectsWithShaders", default.disableScreenEffectsWithShaders),
                disableOffscreenCompositionWithShaders = json.boolean("disableOffscreenCompositionWithShaders", default.disableOffscreenCompositionWithShaders)
            )
        }
    }
}

data class ClientVisuals(
    val particles: Boolean = true,
    val trails: Boolean = true,
    val beams: Boolean = true,
    val auras: Boolean = true,
    val magicCircles: Boolean = true,
    val screenDistortion: Boolean = true,
    val screenGlow: Boolean = true,
    val screenShockwaves: Boolean = true,
    val chromaticShift: Boolean = true,
    val blur: Boolean = true
) {
    companion object {
        fun parse(json: JsonObject?, default: ClientVisuals): ClientVisuals {
            if (json == null) return default
            return ClientVisuals(
                particles = json.boolean("particles", default.particles),
                trails = json.boolean("trails", default.trails),
                beams = json.boolean("beams", default.beams),
                auras = json.boolean("auras", default.auras),
                magicCircles = json.boolean("magicCircles", default.magicCircles),
                screenDistortion = json.boolean("screenDistortion", default.screenDistortion),
                screenGlow = json.boolean("screenGlow", default.screenGlow),
                screenShockwaves = json.boolean("screenShockwaves", default.screenShockwaves),
                chromaticShift = json.boolean("chromaticShift", default.chromaticShift),
                blur = json.boolean("blur", default.blur)
            )
        }
    }
}

data class ClientDebug(
    val showStats: Boolean = false,
    val logReloads: Boolean = true,
    val logSkippedEffects: Boolean = true
) {
    companion object {
        fun parse(json: JsonObject?, default: ClientDebug): ClientDebug {
            if (json == null) return default
            return ClientDebug(
                showStats = json.boolean("showStats", default.showStats),
                logReloads = json.boolean("logReloads", default.logReloads),
                logSkippedEffects = json.boolean("logSkippedEffects", default.logSkippedEffects)
            )
        }
    }
}
