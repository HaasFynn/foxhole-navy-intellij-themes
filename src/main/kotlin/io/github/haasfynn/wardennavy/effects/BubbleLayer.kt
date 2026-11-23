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
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import javax.swing.JComponent
import javax.swing.SwingUtilities
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

/**
 * A see-through layer over one editor in which a few air bubbles drift upwards, as if seen while diving.
 * It never takes mouse or keyboard input and only repaints the narrow column its bubbles rise in.
 */
internal class BubbleLayer(private val editor: Editor) : JComponent() {

    private class Bubble(var x: Double, var y: Double, val radius: Double, val speed: Double,
                         val sway: Double, val swayRate: Double, val burstAt: Double, var age: Double = 0.0) {
        /** Seconds since the bubble burst, or a negative value while it is still rising. */
        var burst = -1.0
        var lastBounds: Rectangle? = null
    }

    private val bubbles = ArrayList<Bubble>()
    private val random = Random.Default
    private var untilNextStream = 0.5
    private var streamX = 0.0
    private var streamLeft = 0

    private val areaListener = VisibleAreaListener { fitToVisibleArea() }

    init {
        isOpaque = false
        isFocusable = false
        editor.contentComponent.add(this)
        editor.scrollingModel.addVisibleAreaListener(areaListener)
        fitToVisibleArea()
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

    /** Advances the simulation by [seconds] and repaints what moved. */
    fun step(seconds: Double) {
        if (!isShowing || width <= 0 || height <= 0) return
        // No animation while the IDE is in the background.
        if (SwingUtilities.getWindowAncestor(this)?.isActive != true && bubbles.isEmpty()) return
        spawn(seconds)

        var dirty: Rectangle? = null
        val iterator = bubbles.iterator()
        while (iterator.hasNext()) {
            val bubble = iterator.next()
            val before = bubble.lastBounds
            bubble.age += seconds
            if (bubble.burst >= 0) {
                bubble.burst += seconds
            } else {
                bubble.y -= bubble.speed * seconds
                if (bubble.y < bubble.burstAt) bubble.burst = 0.0
            }
            val gone = bubble.burst > BURST_SECONDS
            val after = if (gone) null else boundsOf(bubble)
            bubble.lastBounds = after
            if (gone) iterator.remove()
            for (area in listOfNotNull(before, after)) dirty = dirty?.union(area) ?: area
        }
        // All bubbles belong to one narrow stream, so the area to redraw is a slim column
        // rather than the whole editor.
        dirty?.let(::repaint)
    }

    private fun spawn(seconds: Double) {
        if (streamLeft == 0 && bubbles.isEmpty()) {
            untilNextStream -= seconds
            if (untilNextStream <= 0) {
                // A breath of air: a handful of bubbles from one spot.
                streamX = random.nextDouble(width * 0.08, width * 0.92)
                streamLeft = random.nextInt(3, 7)
                untilNextStream = random.nextDouble(2.5, 6.0)
            }
        }
        if (streamLeft > 0 && random.nextDouble() < seconds * 3) {
            streamLeft--
            val radius = JBUI.scale(1) * random.nextDouble(1.5, 4.5)
            bubbles += Bubble(
                x = streamX + random.nextDouble(-6.0, 6.0),
                y = height + radius,
                radius = radius,
                speed = JBUI.scale(1) * (26 + radius * 8) * random.nextDouble(0.85, 1.15),
                sway = random.nextDouble(0.0, 100.0),
                swayRate = random.nextDouble(1.2, 2.2),
                // Bubbles burst at different heights in the upper third rather than on one line.
                burstAt = height * random.nextDouble(0.04, 0.34),
            )
        }
    }

    private fun centreX(bubble: Bubble) = bubble.x + sin(bubble.age * bubble.swayRate + bubble.sway) * bubble.radius * 1.2

    private fun boundsOf(bubble: Bubble): Rectangle {
        val reach = (if (bubble.burst >= 0) bubble.radius * 2.4 else bubble.radius) + 2
        val size = ceil(reach * 2).toInt() + 2
        return Rectangle((centreX(bubble) - reach).toInt() - 1, (bubble.y - reach).toInt() - 1, size, size)
    }

    override fun paintComponent(g: Graphics) {
        if (bubbles.isEmpty()) return
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            val base = JBColor.namedColor("Label.foreground", JBColor.foreground())
            for (bubble in bubbles) paintBubble(g2, bubble, base)
        } finally {
            g2.dispose()
        }
    }

    private fun paintBubble(g2: Graphics2D, bubble: Bubble, base: Color) {
        val x = centreX(bubble)
        val fadeIn = (bubble.age / 0.6).coerceAtMost(1.0)
        if (bubble.burst >= 0) {
            // The burst: a ring that widens and fades.
            val progress = (bubble.burst / BURST_SECONDS).coerceIn(0.0, 1.0)
            val radius = bubble.radius * (1 + 1.3 * progress)
            g2.color = withAlpha(base, STRENGTH * (1 - progress))
            g2.stroke = BasicStroke(JBUI.scale(1).toFloat() * 0.8f)
            g2.draw(Ellipse2D.Double(x - radius, bubble.y - radius, radius * 2, radius * 2))
            return
        }
        val r = bubble.radius
        val shape = Ellipse2D.Double(x - r, bubble.y - r, r * 2, r * 2)
        g2.color = withAlpha(base, STRENGTH * 0.18 * fadeIn)
        g2.fill(shape)
        g2.color = withAlpha(base, STRENGTH * fadeIn)
        g2.stroke = BasicStroke(JBUI.scale(1).toFloat())
        g2.draw(shape)
        if (r > 2.5) {
            // A small glint on the upper left.
            g2.color = withAlpha(base, STRENGTH * 1.4 * fadeIn)
            g2.draw(Arc2D.Double(x - r * 0.55, bubble.y - r * 0.55, r * 1.1, r * 1.1, 110.0, 60.0, Arc2D.OPEN))
        }
    }

    private fun withAlpha(colour: Color, alpha: Double) =
        Color(colour.red, colour.green, colour.blue, (alpha.coerceIn(0.0, 1.0) * 255).toInt())

    private companion object {
        const val BURST_SECONDS = 0.35
        /** Overall opacity of a bubble's outline; the effect is meant to stay in the background. */
        const val STRENGTH = 0.34
    }
}
