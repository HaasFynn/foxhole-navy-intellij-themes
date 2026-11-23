package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import javax.swing.JComponent
import javax.swing.JLayeredPane

/**
 * Two rings spread once across the sonar display in the corner of the window:
 * green when something finished well, red when it failed.
 */
internal class SonarPing private constructor(private val layers: JLayeredPane, success: Boolean) : JComponent(), Effect {
    private val colour: Color = if (success) JBColor.namedColor("ProgressBar.passedColor", JBColor.GREEN)
    else JBColor.namedColor("ProgressBar.failedColor", JBColor.RED)
    private var time = 0.0

    init {
        isOpaque = false
        isFocusable = false
        // The sonar artwork is anchored to the bottom right corner of the window; these are the
        // offset of its centre from that corner and the radius of its outer ring, in image pixels.
        val radius = OUTER_RING + 6
        setBounds(layers.width - CENTRE_OFFSET - radius, layers.height - CENTRE_OFFSET - radius, radius * 2, radius * 2)
        layers.add(this, JLayeredPane.DRAG_LAYER as Any)
    }

    override fun contains(x: Int, y: Int) = false

    override fun step(seconds: Double): Boolean {
        time += seconds
        if (!isShowing) return false
        paintImmediately(0, 0, width, height)
        return time < DURATION
    }

    override fun finish() {
        val area = bounds
        layers.remove(this)
        layers.repaint(area)
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g2.stroke = BasicStroke(JBUI.scale(2).toFloat())
            val centre = width / 2.0
            for (delay in doubleArrayOf(0.0, 0.3)) {
                val progress = ((time - delay) / (DURATION - 0.3)).coerceIn(0.0, 1.0)
                if (progress <= 0.0 || progress >= 1.0) continue
                // Ease out: fast at first, slowing towards the rim.
                val radius = OUTER_RING * (1 - (1 - progress) * (1 - progress))
                g2.color = Color(colour.red, colour.green, colour.blue, (200 * (1 - progress)).toInt())
                g2.draw(Ellipse2D.Double(centre - radius, centre - radius, radius * 2, radius * 2))
            }
        } finally {
            g2.dispose()
        }
    }

    companion object {
        private const val DURATION = 1.5
        private const val CENTRE_OFFSET = 283
        private const val OUTER_RING = 230

        fun play(project: Project, success: Boolean) {
            val frame = WindowManager.getInstance().getFrame(project) ?: return
            if (!frame.isShowing) return
            Effects.play(SonarPing(frame.rootPane.layeredPane, success))
        }
    }
}
