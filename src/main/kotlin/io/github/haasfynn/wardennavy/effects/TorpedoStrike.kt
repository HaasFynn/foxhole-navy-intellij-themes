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
 * A torpedo runs along one line of the editor from [startX] to the left and goes off against the
 * left edge, the "wall". [lineY] is the top of the line and [lineHeight] its height, both relative
 * to the visible area.
 */
internal class TorpedoStrike(editor: Editor, private val lineY: Int, private val lineHeight: Int,
                             private val startX: Double) : EditorOverlay(editor) {

    private class Droplet(val angle: Double, val speed: Double, val size: Double)

    private val scale = JBUI.scale(1).toDouble()
    private val centreY = lineY + lineHeight / 2.0
    private val length = 18 * scale
    private val wake = 70 * scale
    private val runSeconds = (startX / (1700 * scale)).coerceIn(0.2, 0.5)
    private val blastRadius = max(lineHeight * 1.3, 18 * scale)
    /** Where the nose meets the wall. */
    private val impactX = 1 * scale
    // The wall is on the left, so the spray flies back to the right, mostly upwards.
    private val droplets = List(9) {
        Droplet(Math.toRadians(Random.nextDouble(-80.0, 40.0)), scale * Random.nextDouble(70.0, 170.0),
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
            Rectangle(nose.toInt() - 3, lineY - 1, (length + wake).toInt() + 8, lineHeight + 2)
        } else {
            val reach = (blastRadius + 180 * scale * BLAST_SECONDS).toInt() + 4
            Rectangle(0, centreY.toInt() - reach, reach + 4, reach * 2)
        }
        val alive = time < runSeconds + BLAST_SECONDS
        paintImmediately(lastArea?.union(area) ?: area)
        lastArea = if (alive) area else null
        return alive
    }

    private fun noseX() = impactX + (startX - impactX) * (1 - (time / runSeconds).coerceAtMost(1.0))

    override fun paintEffect(g: Graphics2D) {
        if (time < runSeconds) paintTorpedo(g) else paintBlast(g, ((time - runSeconds) / BLAST_SECONDS).coerceIn(0.0, 1.0))
    }

    private fun paintTorpedo(g: Graphics2D) {
        val nose = noseX()
        val tail = nose + length
        val thickness = 4 * scale
        // The wake of bubbles behind the screw.
        g.paint = GradientPaint(tail.toFloat(), 0f, Color(hull.red, hull.green, hull.blue, 110),
            (tail + wake).toFloat(), 0f, Color(hull.red, hull.green, hull.blue, 0))
        g.stroke = BasicStroke(scale.toFloat() * 1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        g.draw(Line2D.Double(tail, centreY, tail + wake, centreY))
        g.color = Color(hull.red, hull.green, hull.blue, 220)
        g.fill(RoundRectangle2D.Double(nose, centreY - thickness / 2, length, thickness, thickness, thickness))
        val fins = Path2D.Double()
        fins.moveTo(tail - 3 * scale, centreY)
        fins.lineTo(tail + 1 * scale, centreY - thickness)
        fins.lineTo(tail + 1 * scale, centreY + thickness)
        fins.closePath()
        g.fill(fins)
        g.color = Color(fire.red, fire.green, fire.blue, 230)
        g.fill(Ellipse2D.Double(nose, centreY - thickness / 2, 4 * scale, thickness))
    }

    private fun paintBlast(g: Graphics2D, progress: Double) {
        val fade = 1 - progress
        val radius = blastRadius * (0.25 + 0.75 * progress)
        // Half of the blast is cut off by the wall, which is what makes it read as an impact.
        g.color = Color(fire.red, fire.green, fire.blue, (110 * fade * fade).toInt())
        g.fill(Ellipse2D.Double(impactX - radius, centreY - radius, radius * 2, radius * 2))
        g.color = Color(fire.red, fire.green, fire.blue, (230 * fade).toInt())
        g.stroke = BasicStroke(scale.toFloat() * 1.6f)
        g.draw(Ellipse2D.Double(impactX - radius, centreY - radius, radius * 2, radius * 2))
        val seconds = progress * BLAST_SECONDS
        g.color = Color(hull.red, hull.green, hull.blue, (210 * fade).toInt())
        for (droplet in droplets) {
            val x = impactX + cos(droplet.angle) * droplet.speed * seconds
            val y = centreY + sin(droplet.angle) * droplet.speed * seconds + 260 * scale * seconds * seconds
            g.fill(Ellipse2D.Double(x - droplet.size, y - droplet.size, droplet.size * 2, droplet.size * 2))
        }
    }

    private companion object {
        const val BLAST_SECONDS = 0.55
    }
}
