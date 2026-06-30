package io.github.yuazer.magicrender.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.Reader
import kotlin.math.max
import kotlin.math.min

fun parseJsonObject(reader: Reader, path: String, result: ConfigLoadAccumulator): JsonObject? {
    return try {
        val element = JsonParser.parseReader(reader)
        if (!element.isJsonObject) {
            result.error(path, "Root must be a JSON object.")
            null
        } else {
            element.asJsonObject
        }
    } catch (exception: Exception) {
        result.error(path, "Invalid JSON: ${exception.message ?: exception.javaClass.simpleName}")
        null
    }
}

fun JsonObject.obj(name: String): JsonObject? {
    val element = get(name) ?: return null
    return if (element.isJsonObject) element.asJsonObject else null
}

fun JsonObject.string(name: String, default: String): String {
    val element = get(name) ?: return default
    return if (element.isJsonPrimitive) element.asString else default
}

fun JsonObject.boolean(name: String, default: Boolean): Boolean {
    val element = get(name) ?: return default
    return if (element.isJsonPrimitive) {
        try {
            element.asBoolean
        } catch (_: Exception) {
            default
        }
    } else {
        default
    }
}

fun JsonObject.int(
    name: String,
    default: Int,
    minValue: Int,
    maxValue: Int,
    path: String,
    result: ConfigLoadAccumulator
): Int {
    val element = get(name) ?: return default
    val parsed = if (element.isJsonPrimitive) {
        try {
            element.asInt
        } catch (_: Exception) {
            result.warning(path, "`$name` must be an integer. Using $default.")
            return default
        }
    } else {
        result.warning(path, "`$name` must be an integer. Using $default.")
        return default
    }
    return parsed.clamp(minValue, maxValue, "$path.$name", result)
}

fun JsonObject.double(
    name: String,
    default: Double,
    minValue: Double,
    maxValue: Double,
    path: String,
    result: ConfigLoadAccumulator
): Double {
    val element = get(name) ?: return default
    val parsed = if (element.isJsonPrimitive) {
        try {
            element.asDouble
        } catch (_: Exception) {
            result.warning(path, "`$name` must be a number. Using $default.")
            return default
        }
    } else {
        result.warning(path, "`$name` must be a number. Using $default.")
        return default
    }
    return parsed.clamp(minValue, maxValue, "$path.$name", result)
}

fun JsonObject.stringList(name: String): List<String> {
    val element = get(name) ?: return emptyList()
    if (!element.isJsonArray) return emptyList()
    return element.asJsonArray.mapNotNull(JsonElement::asStringOrNull)
}

fun JsonObject.warnUnknownFields(knownFields: Set<String>, path: String, result: ConfigLoadAccumulator) {
    for ((name, _) in entrySet()) {
        if (name !in knownFields) {
            result.info(path, "Unknown field `$name` ignored.")
        }
    }
}

fun JsonElement.asStringOrNull(): String? {
    return if (isJsonPrimitive) {
        try {
            asString
        } catch (_: Exception) {
            null
        }
    } else {
        null
    }
}

fun Int.clamp(minValue: Int, maxValue: Int, path: String, result: ConfigLoadAccumulator): Int {
    val clamped = max(minValue, min(this, maxValue))
    if (clamped != this) {
        result.warning(path, "Value $this was clamped to $clamped.")
    }
    return clamped
}

fun Double.clamp(minValue: Double, maxValue: Double, path: String, result: ConfigLoadAccumulator): Double {
    val clamped = max(minValue, min(this, maxValue))
    if (clamped != this) {
        result.warning(path, "Value $this was clamped to $clamped.")
    }
    return clamped
}

fun isValidIdentifier(value: String): Boolean {
    return value.matches(Regex("[a-z0-9_.-]+:[a-z0-9_./-]+"))
}

fun isColorString(value: String): Boolean {
    return value.matches(Regex("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?"))
}
