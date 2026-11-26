package io.github.haasfynn.navy

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.PowerSaveMode
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import io.github.haasfynn.navy.effects.Demo
import io.github.haasfynn.navy.effects.Effects
import io.github.haasfynn.navy.instruments.DepthGaugeWidgetFactory
import io.github.haasfynn.navy.progress.NavalProgressBar

/** Keeps everything in step with theme changes, IDE start and Power Save Mode. */
class NavyListener : LafManagerListener, AppLifecycleListener, PowerSaveMode.Listener {
    override fun lookAndFeelChanged(source: LafManager) {
        NavalProgressBar.sync()
        DepthGaugeWidgetFactory.refresh()
        if (!Navy.effectsAllowed()) Effects.stopAll()
    }

    override fun appFrameCreated(commandLineArgs: List<String>) {
        if (Navy.variant() != null) {
            // The IDE decides whether to lay the window out as islands before a theme from a plugin
            // is in place, and remembers the answer. Applying the theme once more makes it look again;
            // without this the islands are gone after every restart.
            LafManager.getInstance().updateUI()
        }
        NavalProgressBar.sync()
        Demo.startIfRequested()
    }

    override fun powerSaveStateChanged() {
        if (!Navy.effectsAllowed()) Effects.stopAll()
    }
}
