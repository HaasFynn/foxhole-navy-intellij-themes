package io.github.haasfynn.wardennavy.effects

import com.intellij.openapi.editor.Editor
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.BasicStroke
import java.awt.Color
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * A torpedo runs along one line of the editor from the left edge to [targetX] and goes off there.
 * [lineY] is the top of the line and [lineHeight] its height, both relative to the visible area.
 */
internal class TorpedoStrike(editor: Editor, private val lineY: Int, private val lineHeight: Int,
                             private val targetX: Double) : EditorOverlay(editor) {

    private class Droplet(val angle: Double, val speed: Double, val size: Double)

    private val scale = JBUI.scale(1).toDouble()
    private val centreY = lineY + lineHeight / 2.0
    private val length = 18 * scale
    private val wake = 70 * scale
    private val runSeconds = (targetX / (1500 * scale)).coerceIn(0.18, 0.45)
    private val blastRadius = max(lineHeight * 1.3, 18 * scale)
    private val droplets = List(8) {
        Droplet(Math.toRadians(Random.nextDouble(200.0, 340.0)), scale * Random.nextDouble(70.0, 150.0),
            scale * Random.nextDouble(1.2, 2.4))
    }
    private val hull: Color = JBColor.namedColor("Label.foreground", JBColor.foreground())
    private val fire = Color(0xD4, 0xAF, 0x5A)

    private var time = 0.0
    private var lastArea: Rectangle? = null

    override fun advance(seconds: Double): Boolean {
        time += seconds
        val area = if (time < runSeconds) {
            val nose = noseX()
            Rectangle((nose - length - wake).toInt() - 2, lineY - 1, (length + wake).toInt() + 6, lineHeight + 2)
        } else {
            val reach = (blastRadius + 150 * scale * BLAST_SECONDS).toInt() + 4
            Rectangle(targetX.toInt() - reach, centreY.toInt() - reach, reach * 2, reach * 2)
        }
        val alive = time < runSeconds + BLAST_SECONDS
        paintImmediately(lastArea?.union(area) ?: area)
        lastArea = if (alive) area else null
        return alive
    }

    private fun noseX() = targetX * (time / runSeconds).coerceAtMost(1.0)

    override fun paintEffect(g: Graphics2D) {
        if (time < runSeconds) paintTorpedo(g) else paintBlast(g, ((time - runSeconds) / BLAST_SECONDS).coerceIn(0.0, 1.0))
    }

    private fun paintTorpedo(g: Graphics2D) {
        val nose = noseX()
        val tail = nose - length
        val thickness = 4 * scale
        // The wake of bubbles behind the screw.
        g.paint = GradientPaint((tail - wake).toFloat(), 0f, Color(hull.red, hull.green, hull.blue, 0),
            tail.toFloat(), 0f, Color(hull.red, hull.green, hull.blue, 110))
        g.stroke = BasicStroke(scale.toFloat() * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.draw(Line2D.Double(tail - wake, centreY, tail, centreY))
        g.color = Color(hull.red, hull.green, hull.blue, 220)
        g.fill(RoundRectangle2D.Double(tail, centreY - thickness / 2, length, thickness, thickness, thickness))
        val fins = Path2D.Double()
        fins.moveTo(tail + 3 * scale, centreY)
        fins.lineTo(tail - 1 * scale, centreY - thickness)
        fins.lineTo(tail - 1 * scale, centreY + thickness)
        fins.closePath()
        g.fill(fins)
        g.color = Color(fire.red, fire.green, fire.blue, 230)
        g.fill(Ellipse2D.Double(nose - 4 * scale, centreY - thickness / 2, 4 * scale, thickness))
    }

    private fun paintBlast(g: Graphics2D, progress: Double) {
        val fade = 1 - progress
        val radius = blastRadius * (0.25 + 0.75 * progress)
        g.color = Color(fire.red, fire.green, fire.blue, (90 * fade * fade).toInt())
        g.fill(Ellipse2D.Double(targetX - radius, centreY - radius, radius * 2, radius * 2))
        g.color = Color(fire.red, fire.green, fire.blue, (230 * fade).toInt())
        g.stroke = BasicStroke(scale.toFloat() * 1.6f)
        g.draw(Ellipse2D.Double(targetX - radius, centreY - radius, radius * 2, radius * 2))
        // Spray: thrown up and out, then pulled back down.
        val seconds = progress * BLAST_SECONDS
        g.color = Color(hull.red, hull.green, hull.blue, (210 * fade).toInt())
        for (droplet in droplets) {
            val x = targetX + cos(droplet.angle) * droplet.speed * seconds
            val y = centreY + sin(droplet.angle) * droplet.speed * seconds + 260 * scale * seconds * seconds
            g.fill(Ellipse2D.Double(x - droplet.size, y - droplet.size, droplet.size * 2, droplet.size * 2))
        }
    }

    private companion object {
        const val BLAST_SECONDS = 0.55
    }
}
