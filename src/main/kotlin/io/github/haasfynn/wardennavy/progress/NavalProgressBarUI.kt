package io.github.haasfynn.wardennavy.progress

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Color
import java.awt.Dimension
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.ComponentUI
import javax.swing.plaf.basic.BasicProgressBarUI
import kotlin.math.max
import kotlin.math.min

/**
 * Draws progress as a ship steaming along a waterline. Indeterminate progress is a sonar pulse
 * sweeping across the water.
 */
class NavalProgressBarUI : BasicProgressBarUI() {

    override fun getPreferredSize(c: JComponent): Dimension {
        val size = super.getPreferredSize(c)
        return if (isVertical()) size else Dimension(size.width, JBUI.scale(BAR_HEIGHT))
    }

    /**
     * Swing repaints only the moving "box" of an indeterminate bar on each frame. The whole bar is
     * animated here, so the box has to span it; otherwise the pulse updates in fragments.
     */
    override fun getBoxLength(availableLength: Int, otherDimension: Int): Int = availableLength

    override fun paintDeterminate(g: Graphics, c: JComponent) {
        if (isVertical()) return super.paintDeterminate(g, c)
        withGraphics(g) { g2, area ->
            if (area.height < JBUI.scale(MIN_SHIP_BAR)) return@withGraphics paintPlainBar(g2, area, c)
            val ship = scaledShip(area.height)
            val shipWidth = ship.bounds2D.width
            val fraction = progressBar.percentComplete.coerceIn(0.0, 1.0)
            val travelled = (area.width - shipWidth) * fraction
            val forwards = c.componentOrientation.isLeftToRight
            val shipX = if (forwards) area.x + travelled else area.x + area.width - shipWidth - travelled

            val wakeStart = if (forwards) area.x.toDouble() else shipX + shipWidth
            val wakeEnd = if (forwards) shipX else (area.x + area.width).toDouble()
            g2.color = trackColor()
            g2.draw(waterline(area.x.toDouble(), (area.x + area.width).toDouble(), area.waterY, 0.0))
            g2.color = progressColor()
            g2.draw(waterline(wakeStart, wakeEnd, area.waterY, 0.0))

            paintShip(g2, ship, shipX, area.waterY, forwards)
        }
    }

    override fun paintIndeterminate(g: Graphics, c: JComponent) {
        if (isVertical()) return super.paintIndeterminate(g, c)
        withGraphics(g) { g2, area ->
            val phase = (System.currentTimeMillis() % SWEEP_MILLIS) / SWEEP_MILLIS.toDouble()
            g2.color = trackColor()
            g2.draw(waterline(area.x.toDouble(), (area.x + area.width).toDouble(), area.waterY, phase * WAVE_LENGTH * 4))

            // The pulse enters from the left edge, crosses the bar and leaves on the right.
            val pulseWidth = max(area.width / 3.0, JBUI.scale(24).toDouble())
            val head = area.x - pulseWidth + (area.width + 2 * pulseWidth) * phase
            val colour = progressColor()
            val clear = Color(colour.red, colour.green, colour.blue, 0)
            g2.paint = GradientPaint((head - pulseWidth).toFloat(), 0f, clear, head.toFloat(), 0f, colour)
            val from = max(head - pulseWidth, area.x.toDouble())
            val to = min(head, (area.x + area.width).toDouble())
            if (to > from) {
                g2.draw(waterline(from, to, area.waterY, phase * WAVE_LENGTH * 4))
                val ping = JBUI.scale(2).toDouble()
                if (head <= area.x + area.width) {
                    g2.color = colour
                    g2.fill(java.awt.geom.Ellipse2D.Double(head - ping, area.waterY - JBUI.scale(5) - ping, ping * 2, ping * 2))
                }
            }
        }
    }

    /** The band the bar is drawn in: as wide as the component, at most [BAR_HEIGHT] tall and centred. */
    private class Area(val x: Int, val y: Int, val width: Int, val height: Int) {
        val waterY: Double get() = y + height - JBUI.scale(3).toDouble()
    }

    private inline fun withGraphics(g: Graphics, paint: (Graphics2D, Area) -> Unit) {
        val bar = progressBar
        // Like the platform bar: take on the colour of the surroundings instead of a box of our own.
        val parent = bar.parent
        if (bar.isOpaque && parent != null && parent.isOpaque) {
            g.color = parent.background
            g.fillRect(0, 0, bar.width, bar.height)
        }
        val insets = bar.insets
        val available = bar.height - insets.top - insets.bottom
        val g2 = g.create() as Graphics2D
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            // A percentage or message goes beside the bar, where the ship cannot run over it.
            val textWidth = paintLabel(g2)
            val width = bar.width - insets.left - insets.right - textWidth
            if (width <= 0 || available <= 0) return
            val height = min(available, JBUI.scale(BAR_HEIGHT))
            val forwards = bar.componentOrientation.isLeftToRight
            val area = Area(insets.left + if (forwards) 0 else textWidth, insets.top + (available - height) / 2, width, height)
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g2.stroke = java.awt.BasicStroke(JBUI.scale(2).toFloat(), java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND)
            paint(g2, area)
        } finally {
            g2.dispose()
        }
    }

    /** Draws the bar's text at its trailing edge and returns the width to keep free for it. */
    private fun paintLabel(g2: Graphics2D): Int {
        val bar = progressBar
        val text = if (bar.isStringPainted) bar.string.orEmpty() else ""
        if (text.isEmpty()) return 0
        val insets = bar.insets
        val metrics = g2.getFontMetrics(bar.font)
        val textWidth = metrics.stringWidth(text)
        val x = if (bar.componentOrientation.isLeftToRight) bar.width - insets.right - textWidth else insets.left
        val y = insets.top + (bar.height - insets.top - insets.bottom - metrics.height) / 2 + metrics.ascent
        g2.font = bar.font
        g2.color = JBColor.namedColor("Label.infoForeground", hullColor())
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.drawString(text, x, y)
        return textWidth + JBUI.scale(8)
    }

    /** For bars too flat to fit a ship: a plain line that fills up. */
    private fun paintPlainBar(g2: Graphics2D, area: Area, c: JComponent) {
        val y = area.y + area.height / 2.0
        val filled = area.width * progressBar.percentComplete.coerceIn(0.0, 1.0)
        val forwards = c.componentOrientation.isLeftToRight
        val line = java.awt.geom.Line2D.Double(area.x.toDouble(), y, (area.x + area.width).toDouble(), y)
        g2.color = trackColor()
        g2.draw(line)
        if (filled > 0) {
            if (forwards) line.x2 = area.x + filled else line.x1 = area.x + area.width - filled
            g2.color = progressColor()
            g2.draw(line)
        }
    }

    private fun isVertical() = progressBar.orientation == SwingConstants.VERTICAL

    private fun progressColor(): Color = when (progressBar.getClientProperty("ProgressBar.status")) {
        "failed" -> JBColor.namedColor("ProgressBar.failedColor", JBColor.RED)
        "passed" -> JBColor.namedColor("ProgressBar.passedColor", JBColor.GREEN)
        "warning" -> JBColor.namedColor("ProgressBar.warningColor", JBColor.YELLOW)
        else -> JBColor.namedColor("ProgressBar.progressColor", JBColor.BLUE)
    }

    private fun trackColor(): Color = JBColor.namedColor("ProgressBar.trackColor", JBColor.GRAY)

    private fun hullColor(): Color = JBColor.namedColor("Label.foreground", JBColor.foreground())

    /** A gentle wave from [from] to [to]; [shift] moves the crests to animate it. */
    private fun waterline(from: Double, to: Double, y: Double, shift: Double): Path2D {
        val path = Path2D.Double()
        if (to <= from) return path
        val half = JBUI.scale(WAVE_LENGTH) / 2.0
        val amplitude = JBUI.scale(1).toDouble()
        // Align crests to a fixed grid so the two halves of the line meet without a kink.
        var x = Math.floor((from - shift) / half) * half + shift
        var up = (Math.floor((from - shift) / half).toLong() % 2L) == 0L
        path.moveTo(from, y)
        while (x < to) {
            val next = x + half
            val peak = if (up) y - amplitude else y + amplitude
            path.quadTo(x + half / 2, peak, min(next, to), y)
            x = next
            up = !up
        }
        return path
    }

    private fun scaledShip(barHeight: Int): Path2D {
        val scale = (barHeight - JBUI.scale(3)) / SHIP_HEIGHT
        return Path2D.Double(SHIP, AffineTransform.getScaleInstance(scale, scale))
    }

    private fun paintShip(g2: Graphics2D, ship: Path2D, x: Double, waterY: Double, forwards: Boolean) {
        val bounds = ship.bounds2D
        val transform = AffineTransform.getTranslateInstance(x, waterY - bounds.height + JBUI.scale(1))
        if (!forwards) {
            transform.translate(bounds.width, 0.0)
            transform.scale(-1.0, 1.0)
        }
        g2.color = hullColor()
        g2.fill(transform.createTransformedShape(ship))
    }

    companion object {
        private const val BAR_HEIGHT = 14
        /** Below this height the ship would be an unreadable smudge. */
        private const val MIN_SHIP_BAR = 8
        private const val WAVE_LENGTH = 10
        private const val SWEEP_MILLIS = 1800L
        private const val SHIP_HEIGHT = 10.0

        /** Side view of a small warship, bow to the right, on a 24 x 10 grid. */
        private val SHIP: Path2D = Path2D.Double().apply {
            // hull
            moveTo(0.0, 6.0); lineTo(2.0, 10.0); lineTo(21.0, 10.0); lineTo(24.0, 6.0); closePath()
            // superstructure and bridge
            moveTo(7.0, 6.0); lineTo(7.0, 3.5); lineTo(15.0, 3.5); lineTo(15.0, 6.0); closePath()
            // funnels
            moveTo(8.5, 3.5); lineTo(8.5, 0.5); lineTo(10.5, 0.5); lineTo(10.5, 3.5); closePath()
            moveTo(11.5, 3.5); lineTo(11.5, 0.5); lineTo(13.5, 0.5); lineTo(13.5, 3.5); closePath()
            // fore and aft turrets
            moveTo(17.0, 6.0); lineTo(17.0, 4.5); lineTo(20.0, 4.5); lineTo(20.0, 6.0); closePath()
            moveTo(3.0, 6.0); lineTo(3.0, 4.5); lineTo(5.5, 4.5); lineTo(5.5, 6.0); closePath()
        }

        // Swing looks this factory up reflectively; it deliberately hides the inherited static one.
        @JvmStatic
        @Suppress("ACCIDENTAL_OVERRIDE", "UNUSED_PARAMETER")
        fun createUI(c: JComponent): ComponentUI = NavalProgressBarUI()
    }
}
