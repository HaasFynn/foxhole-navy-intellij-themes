package io.github.haasfynn.navy.progress

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import io.github.haasfynn.navy.Variant
import io.github.haasfynn.navy.Navy
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.awt.GradientPaint
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import javax.swing.JComponent
import javax.swing.SwingConstants
import javax.swing.plaf.ComponentUI
import javax.swing.plaf.basic.BasicProgressBarUI
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * Draws progress as a vessel making its way along the water: a ship on the surface in Blacksteele,
 * a submarine running awash in Nakki. For indeterminate progress the vessel crosses the bar again
 * and again, trailing a wake.
 */
class NavalProgressBarUI : BasicProgressBarUI() {

    override fun getPreferredSize(c: JComponent): Dimension {
        val size = super.getPreferredSize(c)
        return if (isVertical()) size else Dimension(size.width, JBUI.scale(BAR_HEIGHT))
    }

    /**
     * Swing repaints only the moving "box" of an indeterminate bar on each frame. The whole bar is
     * animated here, so the box has to span it; otherwise the animation updates in fragments.
     */
    override fun getBoxLength(availableLength: Int, otherDimension: Int): Int = availableLength

    override fun paintDeterminate(g: Graphics, c: JComponent) {
        if (isVertical()) return super.paintDeterminate(g, c)
        paintBar(g) { scene ->
            val travelled = (scene.width - scene.vesselWidth) * progressBar.percentComplete.coerceIn(0.0, 1.0)
            val stern = if (scene.forwards) scene.left + travelled else scene.right - scene.vesselWidth - travelled
            scene.g.color = trackColor()
            scene.g.draw(scene.water(scene.left, scene.right, 0.0))
            // The stretch already covered lies behind the vessel.
            scene.g.color = progressColor()
            if (scene.forwards) scene.g.draw(scene.water(scene.left, stern, 0.0))
            else scene.g.draw(scene.water(stern + scene.vesselWidth, scene.right, 0.0))
            scene.paintVessel(stern)
        }
    }

    override fun paintIndeterminate(g: Graphics, c: JComponent) {
        if (isVertical()) return super.paintIndeterminate(g, c)
        paintBar(g) { scene ->
            val phase = (System.currentTimeMillis() % SWEEP_MILLIS) / SWEEP_MILLIS.toDouble()
            val shift = phase * JBUI.scale(WAVE_LENGTH) * 4
            scene.g.color = trackColor()
            scene.g.draw(scene.water(scene.left, scene.right, shift))

            val wake = max(scene.width / 3.0, JBUI.scale(24).toDouble())
            val travel = (scene.width + scene.vesselWidth + wake) * phase
            val stern = if (scene.forwards) scene.left - scene.vesselWidth + travel else scene.right - travel
            val wakeNear = if (scene.forwards) stern else stern + scene.vesselWidth
            val wakeFar = if (scene.forwards) stern - wake else stern + scene.vesselWidth + wake
            val colour = progressColor()
            scene.g.paint = GradientPaint(wakeFar.toFloat(), 0f, Color(colour.red, colour.green, colour.blue, 0),
                wakeNear.toFloat(), 0f, colour)
            val from = max(min(wakeFar, wakeNear), scene.left)
            val to = min(max(wakeFar, wakeNear), scene.right)
            if (to > from) scene.g.draw(scene.water(from, to, shift))

            val clip = scene.g.clip
            scene.g.clipRect(scene.left.toInt(), 0, scene.width.toInt(), progressBar.height)
            scene.paintVessel(stern)
            scene.g.clip = clip
        }
    }

    /** Everything needed to draw one bar: where the water is and how large the vessel comes out. */
    private inner class Scene(val g: Graphics2D, val left: Double, top: Double, val width: Double, height: Double) {
        val right = left + width
        val forwards = progressBar.componentOrientation.isLeftToRight
        /** Bars too flat for waves get a straight waterline and a small vessel. */
        private val flat = height < JBUI.scale(MIN_WAVE_BAR)
        private val margin = JBUI.scale(if (flat) 1 else 3).toDouble()
        private val scale = (height - margin) / Vessel.GRID_HEIGHT
        private val vessel = if (Navy.variant() == Variant.NAKKI) Vessel.SUBMARINE else Vessel.SHIP
        private val vesselTop = top + if (flat) 0.0 else JBUI.scale(1).toDouble()
        private val waterY = vesselTop + vessel.waterline * scale
        private val scaled: Shape = AffineTransform.getScaleInstance(scale, scale).createTransformedShape(vessel.shape)
        val vesselWidth = scaled.bounds2D.width

        init {
            g.stroke = BasicStroke(JBUI.scale(1).toFloat() * (if (flat) 1.5f else 2f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        }

        /** The water surface from [from] to [to]; [shift] moves the crests to animate it. */
        fun water(from: Double, to: Double, shift: Double): Shape =
            if (flat || to <= from) Line2D.Double(from, waterY, max(from, to), waterY) else waves(from, to, waterY, shift)

        fun paintVessel(stern: Double) {
            val transform = AffineTransform.getTranslateInstance(stern, vesselTop)
            if (!forwards) {
                transform.translate(vesselWidth, 0.0)
                transform.scale(-1.0, 1.0)
            }
            g.color = hullColor()
            g.fill(transform.createTransformedShape(scaled))
        }
    }

    private inline fun paintBar(g: Graphics, paint: (Scene) -> Unit) {
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
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            // A percentage or message goes beside the bar, where the vessel cannot run over it.
            val textWidth = paintLabel(g2)
            val width = bar.width - insets.left - insets.right - textWidth
            if (width <= 0 || available <= 0) return
            // The bar is at most BAR_HEIGHT tall and centred in whatever room it was given.
            val height = min(available, JBUI.scale(BAR_HEIGHT))
            val left = insets.left + if (bar.componentOrientation.isLeftToRight) 0 else textWidth
            paint(Scene(g2, left.toDouble(), insets.top + (available - height) / 2.0, width.toDouble(), height.toDouble()))
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

    private fun isVertical() = progressBar.orientation == SwingConstants.VERTICAL

    private fun progressColor(): Color = when (progressBar.getClientProperty("ProgressBar.status")) {
        "failed" -> JBColor.namedColor("ProgressBar.failedColor", JBColor.RED)
        "passed" -> JBColor.namedColor("ProgressBar.passedColor", JBColor.GREEN)
        "warning" -> JBColor.namedColor("ProgressBar.warningColor", JBColor.YELLOW)
        else -> JBColor.namedColor("ProgressBar.progressColor", JBColor.BLUE)
    }

    private fun trackColor(): Color = JBColor.namedColor("ProgressBar.trackColor", JBColor.GRAY)

    private fun hullColor(): Color = JBColor.namedColor("Label.foreground", JBColor.foreground())

    /** Gentle waves from [from] to [to] at height [y]. */
    private fun waves(from: Double, to: Double, y: Double, shift: Double): Path2D {
        val path = Path2D.Double()
        val half = JBUI.scale(WAVE_LENGTH) / 2.0
        val amplitude = JBUI.scale(1).toDouble()
        // Crests sit on a fixed grid, so two stretches of water meet without a kink.
        val first = floor((from - shift) / half)
        var x = first * half + shift
        var up = first.toLong() % 2L == 0L
        path.moveTo(from, y)
        while (x < to) {
            val next = x + half
            path.quadTo(x + half / 2, if (up) y - amplitude else y + amplitude, min(next, to), y)
            x = next
            up = !up
        }
        return path
    }

    companion object {
        private const val BAR_HEIGHT = 14
        /** Below this height there is no room for waves. */
        private const val MIN_WAVE_BAR = 8
        private const val WAVE_LENGTH = 10
        private const val SWEEP_MILLIS = 1800L

        // Swing looks this factory up reflectively; it deliberately hides the inherited static one.
        @JvmStatic
        @Suppress("ACCIDENTAL_OVERRIDE", "UNUSED_PARAMETER")
        fun createUI(c: JComponent): ComponentUI = NavalProgressBarUI()
    }
}
