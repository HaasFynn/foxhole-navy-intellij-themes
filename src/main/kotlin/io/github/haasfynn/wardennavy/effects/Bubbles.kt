package io.github.haasfynn.wardennavy.effects

import com.intellij.ide.PowerSaveMode
import com.intellij.ide.ui.LafManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.EditorKind
import javax.swing.Timer

/**
 * Rising air bubbles in the editors of the Nakki theme.
 * One timer drives all editors and only runs while the effect is switched on.
 */
object Bubbles {
    private const val ENABLED_KEY = "io.github.haasfynn.wardennavy.bubbles"
    private const val NAKKI_THEME_ID = "io.github.haasfynn.wardennavy.nakki"
    private const val FRAME_MILLIS = 66

    private val layers = HashMap<Editor, BubbleLayer>()
    private var lastTick = 0L
    private val timer = Timer(FRAME_MILLIS) { tick() }

    var enabled: Boolean
        get() = PropertiesComponent.getInstance().getBoolean(ENABLED_KEY, true)
        set(value) = PropertiesComponent.getInstance().setValue(ENABLED_KEY, value, true)

    private fun wanted(): Boolean =
        enabled && !PowerSaveMode.isEnabled() &&
            LafManager.getInstance().currentUIThemeLookAndFeel?.id == NAKKI_THEME_ID

    /** Brings the layers in line with the current theme and settings. Call on the event dispatch thread. */
    fun sync() {
        if (wanted()) {
            EditorFactory.getInstance().allEditors.forEach(::attach)
            if (!timer.isRunning) {
                lastTick = System.nanoTime()
                timer.start()
            }
        } else {
            shutDown()
        }
    }

    fun editorCreated(editor: Editor) {
        if (wanted()) {
            attach(editor)
            sync()
        }
    }

    fun editorReleased(editor: Editor) {
        layers.remove(editor)?.detach()
    }

    fun shutDown() {
        timer.stop()
        layers.values.forEach(BubbleLayer::detach)
        layers.clear()
    }

    private fun attach(editor: Editor) {
        if (editor.editorKind != EditorKind.MAIN_EDITOR || editor.isDisposed || editor in layers) return
        layers[editor] = BubbleLayer(editor)
    }

    private fun tick() {
        val now = System.nanoTime()
        // Long gaps (a busy IDE, a sleeping laptop) must not make the bubbles jump.
        val seconds = ((now - lastTick) / 1e9).coerceAtMost(0.1)
        lastTick = now
        if (PowerSaveMode.isEnabled()) return shutDown()
        layers.values.forEach { it.step(seconds) }
    }
}
