package io.github.haasfynn.wardennavy.progress

import com.intellij.ide.AppLifecycleListener
import com.intellij.ide.ui.LafManager
import com.intellij.ide.ui.LafManagerListener

/** Keeps the progress bar in step with theme changes and IDE start. */
class NavalProgressBarListener : LafManagerListener, AppLifecycleListener {
    override fun lookAndFeelChanged(source: LafManager) = NavalProgressBar.sync()

    override fun appFrameCreated(commandLineArgs: List<String>) = NavalProgressBar.sync()
}
