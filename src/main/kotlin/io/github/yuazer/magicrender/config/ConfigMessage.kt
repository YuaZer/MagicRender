package io.github.yuazer.magicrender.config

enum class ConfigMessageLevel {
    INFO,
    WARNING,
    ERROR
}

data class ConfigMessage(
    val level: ConfigMessageLevel,
    val path: String,
    val message: String
)

