package io.github.haasfynn.navy.progress

import io.github.haasfynn.navy.Navy
import io.github.haasfynn.navy.settings.NavySettings
import javax.swing.UIManager

/** Switches the naval progress bar on while a Warden Navy theme is active, and off otherwise. */
object NavalProgressBar {
    private const val UI_KEY = "ProgressBarUI"
    private const val PLATFORM_UI = "com.intellij.ide.ui.laf.darcula.ui.DarculaProgressBarUI"

    fun sync() {
        if (NavySettings.navalProgressBar && Navy.variant() != null) install() else uninstall()
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
