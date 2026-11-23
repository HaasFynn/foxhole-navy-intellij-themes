package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.editor.Editor
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import kotlin.math.ceil
import kotlin.math.sin
import kotlin.random.Random

/** A few air bubbles that rise a short way from ([startX], [startY]) and burst. */
internal class BubbleBurst(editor: Editor, startX: Double, startY: Double) : EditorOverlay(editor) {

    private class Bubble(val x: Double, var y: Double, val radius: Double, val speed: Double,
                         val sway: Double, val swayRate: Double, val burstAt: Double, var delay: Double) {
        var age = 0.0
        /** Seconds since the bubble burst, or negative while it is still rising. */
        var burst = -1.0
        val drawn = Rectangle()
        var visible = false
    }

    private val base: Color = JBColor.namedColor("Label.foreground", JBColor.foreground())
    private val outline = Color(base.red, base.green, base.blue, (STRENGTH * 255).toInt())
    private val body = Color(base.red, base.green, base.blue, (STRENGTH * 0.2 * 255).toInt())
    private val stroke = BasicStroke(JBUI.scale(1).toFloat())
    private val shape = Ellipse2D.Double()
    private val scratch = Rectangle()

    private val bubbles: List<Bubble> = run {
        val random = Random.Default
        val scale = JBUI.scale(1).toDouble()
        List(random.nextInt(2, 5)) { index ->
            val radius = scale * random.nextDouble(1.5, 4.0)
            Bubble(
                x = startX + scale * random.nextDouble(-5.0, 5.0),
                y = startY + scale * random.nextDouble(-3.0, 3.0),
                radius = radius,
                speed = scale * (55 + radius * 14) * random.nextDouble(0.85, 1.15),
                sway = random.nextDouble(0.0, 6.28),
                swayRate = random.nextDouble(2.0, 3.5),
                burstAt = startY - scale * random.nextDouble(70.0, 190.0),
                delay = index * random.nextDouble(0.1, 0.25),
            )
        }
    }

    override fun advance(seconds: Double): Boolean {
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
        paintImmediately(area)
    }

    private fun centreX(bubble: Bubble) = bubble.x + sin(bubble.age * bubble.swayRate + bubble.sway) * bubble.radius * 0.9

    override fun paintEffect(g: Graphics2D) {
        g.stroke = stroke
        val clip = g.clipBounds
        for (bubble in bubbles) {
            if (!bubble.visible || bubble.delay > 0) continue
            if (clip != null && !clip.intersects(bubble.drawn)) continue
            val x = centreX(bubble)
            if (bubble.burst >= 0) {
                // The burst: a ring that widens and fades.
                val progress = (bubble.burst / BURST_SECONDS).coerceIn(0.0, 1.0)
                val radius = bubble.radius * (1 + 1.3 * progress)
                shape.setFrame(x - radius, bubble.y - radius, radius * 2, radius * 2)
                g.color = Color(base.red, base.green, base.blue, (STRENGTH * (1 - progress) * 255).toInt())
                g.draw(shape)
            } else {
                val r = bubble.radius
                shape.setFrame(x - r, bubble.y - r, r * 2, r * 2)
                g.color = body
                g.fill(shape)
                g.color = outline
                g.draw(shape)
            }
        }
    }

    private companion object {
        const val BURST_SECONDS = 0.3
        /** Opacity of a bubble's outline; the effect is meant to stay in the background. */
        const val STRENGTH = 0.4
    }
}
