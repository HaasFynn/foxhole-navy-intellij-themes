package io.github.haasfynn.navy

import com.intellij.ide.PowerSaveMode
import com.intellij.ide.ui.LafManager

/** Which of the plugin's themes is active, if any. */
enum class Variant { BLACKSTEELE, NAKKI }

object Navy {
    const val PLUGIN_ID = "io.github.haasfynn.navy"
    private const val THEME_ID_PREFIX = "$PLUGIN_ID."

    fun variant(): Variant? {
        val id = LafManager.getInstance().currentUIThemeLookAndFeel?.id ?: return null
        if (!id.startsWith(THEME_ID_PREFIX)) return null
        return if (id.endsWith(".nakki")) Variant.NAKKI else Variant.BLACKSTEELE
    }

    /** Animated effects only run under one of our themes and never in Power Save Mode. */
    fun effectsAllowed(): Boolean = variant() != null && !PowerSaveMode.isEnabled()
}
