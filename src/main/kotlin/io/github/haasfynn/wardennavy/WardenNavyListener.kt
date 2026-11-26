package io.github.haasfynn.wardennavy

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.PowerSaveMode
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import io.github.haasfynn.wardennavy.effects.Demo
import io.github.haasfynn.wardennavy.effects.Effects
import io.github.haasfynn.wardennavy.instruments.DepthGaugeWidgetFactory
import io.github.haasfynn.wardennavy.progress.NavalProgressBar

/** Keeps everything in step with theme changes, IDE start and Power Save Mode. */
class WardenNavyListener : LafManagerListener, AppLifecycleListener, PowerSaveMode.Listener {
    override fun lookAndFeelChanged(source: LafManager) {
        NavalProgressBar.sync()
        DepthGaugeWidgetFactory.refresh()
        if (!WardenNavy.effectsAllowed()) Effects.stopAll()
    }

    override fun appFrameCreated(commandLineArgs: List<String>) {
        if (WardenNavy.variant() != null) {
            // The IDE decides whether to lay the window out as islands before a theme from a plugin
            // is in place, and remembers the answer. Applying the theme once more makes it look again;
            // without this the islands are gone after every restart.
            LafManager.getInstance().updateUI()
        }
        NavalProgressBar.sync()
        Demo.startIfRequested()
    }

    override fun powerSaveStateChanged() {
        if (!WardenNavy.effectsAllowed()) Effects.stopAll()
    }
}
