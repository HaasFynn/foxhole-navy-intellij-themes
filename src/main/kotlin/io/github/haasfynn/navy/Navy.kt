package io.github.haasfynn.navy

import com.intellij.ide.PowerSaveMode
import com.intellij.ide.ui.LafManager

enum class Faction { WARDEN, COLONIAL }

/**
 * The plugin's themes. [submerged] tells the surface ships from the submarines, which changes
 * the vessel shown in the progress bar.
 */
enum class Variant(val idSuffix: String, val faction: Faction, val submerged: Boolean) {
    BLACKSTEELE("blacksteele", Faction.WARDEN, submerged = false),
    NAKKI("nakki", Faction.WARDEN, submerged = true),
    CONQUEROR("conqueror", Faction.COLONIAL, submerged = false),
    TRIDENT("trident", Faction.COLONIAL, submerged = true),
}

object Navy {
    const val PLUGIN_ID = "io.github.haasfynn.navy"
    private const val THEME_ID_PREFIX = "$PLUGIN_ID."

    /** Which of the plugin's themes is active, if any. */
    fun variant(): Variant? {
        val id = LafManager.getInstance().currentUIThemeLookAndFeel?.id ?: return null
        if (!id.startsWith(THEME_ID_PREFIX)) return null
        val suffix = id.removePrefix(THEME_ID_PREFIX)
        return Variant.entries.firstOrNull { it.idSuffix == suffix }
    }

    /** Animated effects only run under one of our themes and never in Power Save Mode. */
    fun effectsAllowed(): Boolean = variant() != null && !PowerSaveMode.isEnabled()
}
