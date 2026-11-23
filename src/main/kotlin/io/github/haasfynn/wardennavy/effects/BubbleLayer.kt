package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import javax.swing.JComponent
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

/**
 * A see-through layer over one editor that shows a single breath of air: a few bubbles that rise
 * a short way and burst. It exists only while that breath lasts, never takes mouse or keyboard
 * input, and redraws just the few pixels around each bubble.
 */
internal class BubbleLayer(val editor: Editor) : JComponent() {

    private class Bubble(val x: Double, var y: Double, val radius: Double, val speed: Double,
                         val sway: Double, val swayRate: Double, val burstAt: Double, var delay: Double) {
        var age = 0.0
        /** Seconds since the bubble burst, or negative while it is still rising. */
        var burst = -1.0
        val drawn = Rectangle()
        var visible = false
    }

    private val random = Random.Default
    private val bubbles: List<Bubble>
    private val areaListener = VisibleAreaListener { fitToVisibleArea() }
    private val base: Color = JBColor.namedColor("Label.foreground", JBColor.foreground())
    private val outline = Color(base.red, base.green, base.blue, (STRENGTH * 255).toInt())
    private val body = Color(base.red, base.green, base.blue, (STRENGTH * 0.2 * 255).toInt())
    private val stroke = BasicStroke(JBUI.scale(1).toFloat())
    private val shape = Ellipse2D.Double()
    private val scratch = Rectangle()

    init {
        isOpaque = false
        isFocusable = false
        editor.contentComponent.add(this)
        editor.scrollingModel.addVisibleAreaListener(areaListener)
        fitToVisibleArea()

        val scale = JBUI.scale(1).toDouble()
        val startX = random.nextDouble(width * 0.1, width * 0.9)
        val startY = height * random.nextDouble(0.45, 0.95)
        bubbles = List(random.nextInt(3, 7)) { index ->
            val radius = scale * random.nextDouble(1.5, 4.5)
            Bubble(
                x = startX + scale * random.nextDouble(-7.0, 7.0),
                y = startY + scale * random.nextDouble(-6.0, 6.0),
                radius = radius,
                speed = scale * (60 + radius * 14) * random.nextDouble(0.85, 1.15),
                sway = random.nextDouble(0.0, 6.28),
                swayRate = random.nextDouble(2.0, 3.5),
                burstAt = startY - scale * random.nextDouble(170.0, 360.0),
                delay = index * random.nextDouble(0.12, 0.3),
            )
        }
    }

    fun detach() {
        editor.scrollingModel.removeVisibleAreaListener(areaListener)
        val parent = editor.contentComponent
        val area = bounds
        parent.remove(this)
        parent.repaint(area)
    }

    /** Mouse events fall through to the editor underneath. */
    override fun contains(x: Int, y: Int) = false

    private fun fitToVisibleArea() {
        val area = editor.scrollingModel.visibleArea
        if (bounds != area) bounds = area
    }

    /** Advances the bubbles by [seconds]. Returns false once the last one has burst. */
    fun step(seconds: Double): Boolean {
        if (editor.isDisposed || !isShowing) return false
        var alive = false
        for (bubble in bubbles) {
            if (bubble.burst > BURST_SECONDS) continue
            alive = true
            if (bubble.delay > 0) {
                bubble.delay -= seconds
                continue
            }
            bubble.age += seconds
            if (bubble.burst >= 0) {
                bubble.burst += seconds
            } else {
                bubble.y -= bubble.speed * seconds
                if (bubble.y < bubble.burstAt || bubble.y < bubble.radius * 3) bubble.burst = 0.0
            }
            redraw(bubble)
        }
        return alive
    }

    /** Repaints the bubble's old and new position at once, as one small rectangle. */
    private fun redraw(bubble: Bubble) {
        val gone = bubble.burst > BURST_SECONDS
        val reach = (if (bubble.burst >= 0) bubble.radius * 2.4 else bubble.radius) + 2
        val size = ceil(reach * 2).toInt() + 2
        scratch.setBounds((centreX(bubble) - reach).toInt() - 1, (bubble.y - reach).toInt() - 1, size, size)
        val area = if (bubble.visible) scratch.union(bubble.drawn) else Rectangle(scratch)
        bubble.drawn.bounds = scratch
        bubble.visible = !gone
        // Painted straight away: Swing would otherwise merge all bubbles into one large rectangle
        // and redraw every line of code in between.
        paintImmediately(area)
    }

    private fun centreX(bubble: Bubble) = bubble.x + sin(bubble.age * bubble.swayRate + bubble.sway) * bubble.radius * 0.9

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g2.stroke = stroke
            val clip = g.clipBounds
            for (bubble in bubbles) {
                if (!bubble.visible || bubble.delay > 0) continue
                if (clip != null && !clip.intersects(bubble.drawn)) continue
                paintBubble(g2, bubble)
            }
        } finally {
            g2.dispose()
        }
    }

    private fun paintBubble(g2: Graphics2D, bubble: Bubble) {
        val x = centreX(bubble)
        if (bubble.burst >= 0) {
            // The burst: a ring that widens and fades.
            val progress = (bubble.burst / BURST_SECONDS).coerceIn(0.0, 1.0)
            val radius = bubble.radius * (1 + 1.3 * progress)
            shape.setFrame(x - radius, bubble.y - radius, radius * 2, radius * 2)
            g2.color = Color(base.red, base.green, base.blue, (STRENGTH * (1 - progress) * 255).toInt())
            g2.draw(shape)
            return
        }
        val r = bubble.radius
        shape.setFrame(x - r, bubble.y - r, r * 2, r * 2)
        g2.color = body
        g2.fill(shape)
        g2.color = outline
        g2.draw(shape)
    }

    private companion object {
        const val BURST_SECONDS = 0.3
        /** Opacity of a bubble's outline; the effect is meant to stay in the background. */
        const val STRENGTH = 0.36
    }
}
