package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.VisibleAreaListener
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JComponent

/**
 * A see-through layer over the visible part of one editor that lives only as long as its effect.
 * It never takes mouse or keyboard input. Subclasses repaint small rectangles with
 * [paintImmediately]: Swing would otherwise merge them into one large area and redraw all the
 * code in between on every frame.
 */
internal abstract class EditorOverlay(protected val editor: Editor) : JComponent(), Effect {
    private val areaListener = VisibleAreaListener { fitToVisibleArea() }

    init {
        isOpaque = false
        isFocusable = false
        editor.contentComponent.add(this)
        editor.scrollingModel.addVisibleAreaListener(areaListener)
        fitToVisibleArea()
    }

    final override fun step(seconds: Double): Boolean =
        !editor.isDisposed && isShowing && advance(seconds)

    protected abstract fun advance(seconds: Double): Boolean

    protected abstract fun paintEffect(g: Graphics2D)

    override fun finish() {
        editor.scrollingModel.removeVisibleAreaListener(areaListener)
        val parent = parent ?: return
        val area = bounds
        parent.remove(this)
        parent.repaint(area.x, area.y, area.width, area.height)
    }

    /** Mouse events fall through to the editor underneath. */
    override fun contains(x: Int, y: Int) = false

    private fun fitToVisibleArea() {
        val area = editor.scrollingModel.visibleArea
        if (bounds != area) bounds = area
    }

    final override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            paintEffect(g2)
        } finally {
            g2.dispose()
        }
    }
}
