package io.github.haasfynn.wardennavy.progress

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener
import io.github.haasfynn.wardennavy.effects.Bubbles

/** Keeps the progress bar and the bubble effect in step with theme changes and IDE start. */
class NavalProgressBarListener : LafManagerListener, AppLifecycleListener {
    override fun lookAndFeelChanged(source: LafManager) = syncAll()

    override fun appFrameCreated(commandLineArgs: List<String>) = syncAll()

    private fun syncAll() {
        NavalProgressBar.sync()
        Bubbles.sync()
    }
}
