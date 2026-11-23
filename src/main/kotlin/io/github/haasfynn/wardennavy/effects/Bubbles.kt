package io.github.haasfynn.wardennavy.effects

import com.intellij.ide.PowerSaveMode
import com.intellij.ide.ui.LafManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import javax.swing.Timer
import kotlin.random.Random

/**
 * Now and then a breath of air bubbles rises in the editor that has the keyboard focus.
 *
 * Animating over an editor is costly, so the effect is built to be idle almost all the time:
 * a breath lasts a few seconds, and between breaths no timer ticks and no layer exists.
 */
object Bubbles {
    private const val ENABLED_KEY = "io.github.haasfynn.wardennavy.bubbles"
    private const val THEME_ID_PREFIX = "io.github.haasfynn.wardennavy."
    private const val FRAME_MILLIS = 16

    /** Seconds of quiet between two breaths. */
    private val PAUSE = 25.0..50.0
    private const val FIRST_PAUSE = 6.0

    /** For previews: `-Dwardennavy.bubbles.demo=true` shows breaths every few seconds in any visible editor. */
    private val demo = java.lang.Boolean.getBoolean("wardennavy.bubbles.demo")

    private var layer: BubbleLayer? = null
    private var lastFrame = 0L
    private val frames = Timer(FRAME_MILLIS) { frame() }
    private val pause = Timer(0) { breathe() }.apply { isRepeats = false }

    var enabled: Boolean
        get() = PropertiesComponent.getInstance().getBoolean(ENABLED_KEY, true)
        set(value) = PropertiesComponent.getInstance().setValue(ENABLED_KEY, value, true)

    private fun wanted(): Boolean =
        enabled && !PowerSaveMode.isEnabled() &&
            LafManager.getInstance().currentUIThemeLookAndFeel?.id.orEmpty().startsWith(THEME_ID_PREFIX)

    /** Brings the effect in line with the current theme and settings. Call on the event dispatch thread. */
    fun sync() {
        if (!wanted()) return shutDown()
        if (layer == null && !pause.isRunning) rest(FIRST_PAUSE)
    }

    fun shutDown() {
        pause.stop()
        endBreath()
    }

    private fun rest(seconds: Double) {
        pause.initialDelay = (seconds * 1000).toInt()
        pause.restart()
    }

    private fun breathe() {
        if (!wanted()) return
        val editor = target()
        if (editor == null) {
            // Nobody is in an editor right now; look again later.
            return rest(if (demo) 2.0 else 10.0)
        }
        layer = BubbleLayer(editor)
        lastFrame = System.nanoTime()
        frames.start()
    }

    private fun target(): Editor? {
        val editors = EditorFactory.getInstance().allEditors
            .filter { it.editorKind == EditorKind.MAIN_EDITOR && !it.isDisposed && it.contentComponent.isShowing }
            .filter { it.scrollingModel.visibleArea.run { width > 100 && height > 100 } }
        return editors.firstOrNull { it.contentComponent.isFocusOwner } ?: editors.firstOrNull().takeIf { demo }
    }

    private fun frame() {
        val now = System.nanoTime()
        // Long gaps (a busy IDE, a sleeping laptop) must not make the bubbles jump.
        val seconds = ((now - lastFrame) / 1e9).coerceAtMost(0.05)
        lastFrame = now
        if (layer?.step(seconds) != true) {
            endBreath()
            if (wanted()) rest(if (demo) 3.0 else Random.nextDouble(PAUSE.start, PAUSE.endInclusive))
        }
    }

    private fun endBreath() {
        frames.stop()
        layer?.detach()
        layer = null
    }
}
