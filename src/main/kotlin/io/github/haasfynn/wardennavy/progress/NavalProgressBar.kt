package io.github.haasfynn.wardennavy.progress

import com.intellij.ide.util.PropertiesComponent
import com.intellij.ide.ui.LafManager
import javax.swing.UIManager

/** Switches the naval progress bar on while a Warden Navy theme is active, and off otherwise. */
object NavalProgressBar {
    private const val UI_KEY = "ProgressBarUI"
    private const val ENABLED_KEY = "io.github.haasfynn.wardennavy.navalProgressBar"
    private const val THEME_ID_PREFIX = "io.github.haasfynn.wardennavy."
    private const val PLATFORM_UI = "com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI"

    var enabled: Boolean
        get() = PropertiesComponent.getInstance().getBoolean(ENABLED_KEY, true)
        set(value) = PropertiesComponent.getInstance().setValue(ENABLED_KEY, value, true)

    fun sync() {
        val themeId = LafManager.getInstance().currentUIThemeLookAndFeel?.id.orEmpty()
        if (enabled && themeId.startsWith(THEME_ID_PREFIX)) install() else uninstall()
    }

    fun uninstall() {
        val ours = NavalProgressBarUI::class.java.name
        if (UIManager.get(UI_KEY) == ours) UIManager.put(UI_KEY, PLATFORM_UI)
        UIManager.getDefaults().remove(ours)
    }

    private fun install() {
        val ours = NavalProgressBarUI::class.java
        UIManager.put(UI_KEY, ours.name)
        // Swing resolves the UI class through this entry; without it the plugin class is not found.
        UIManager.getDefaults()[ours.name] = ours
    }
}
