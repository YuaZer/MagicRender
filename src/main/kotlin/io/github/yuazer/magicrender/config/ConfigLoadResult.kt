package io.github.yuazer.magicrender.config

data class ConfigLoadResult(
    val success: Boolean,
    val loadedFiles: Int,
    val skippedEffects: Int,
    val messages: List<ConfigMessage>
) {
    val warnings: List<ConfigMessage>
        get() = messages.filter { it.level == ConfigMessageLevel.WARNING }

    val errors: List<ConfigMessage>
        get() = messages.filter { it.level == ConfigMessageLevel.ERROR }

    fun summary(): String {
        return "success=$success, loadedFiles=$loadedFiles, skippedEffects=$skippedEffects, warnings=${warnings.size}, errors=${errors.size}"
    }

    companion object {
        val EMPTY = ConfigLoadResult(
            success = true,
            loadedFiles = 0,
            skippedEffects = 0,
            messages = emptyList()
        )
    }
}

class ConfigLoadAccumulator {
    var loadedFiles: Int = 0
    var skippedEffects: Int = 0
    private val mutableMessages = mutableListOf<ConfigMessage>()

    val messages: List<ConfigMessage>
        get() = mutableMessages

    val warnings: List<ConfigMessage>
        get() = mutableMessages.filter { it.level == ConfigMessageLevel.WARNING }

    val errors: List<ConfigMessage>
        get() = mutableMessages.filter { it.level == ConfigMessageLevel.ERROR }

    fun info(path: String, message: String) {
        mutableMessages += ConfigMessage(ConfigMessageLevel.INFO, path, message)
    }

    fun warning(path: String, message: String) {
        mutableMessages += ConfigMessage(ConfigMessageLevel.WARNING, path, message)
    }

    fun error(path: String, message: String) {
        mutableMessages += ConfigMessage(ConfigMessageLevel.ERROR, path, message)
    }

    fun toResult(success: Boolean): ConfigLoadResult {
        return ConfigLoadResult(
            success = success,
            loadedFiles = loadedFiles,
            skippedEffects = skippedEffects,
            messages = mutableMessages.toList()
        )
    }
}
