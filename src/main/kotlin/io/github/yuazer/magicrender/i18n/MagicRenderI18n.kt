package io.github.yuazer.magicrender.i18n

import net.minecraft.network.chat.Component

object MagicRenderI18n {
    fun tr(key: String, vararg args: Any): Component {
        return Component.translatable(key, *args)
    }

    fun key(suffix: String): String = "magicrender.$suffix"
}
