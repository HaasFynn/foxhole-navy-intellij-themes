package io.github.haasfynn.navy.progress

import io.github.haasfynn.navy.Variant
import java.awt.geom.Path2D

/**
 * A side view, bow to the right, on a grid [GRID_HEIGHT] units tall.
 * [waterline] is where the water surface crosses the grid: the bottom for a ship that sits on the
 * water, part way up for a submarine that runs awash.
 */
internal class Vessel(val shape: Path2D, val waterline: Double) {
    companion object {
        const val GRID_HEIGHT = 10.0

        fun of(variant: Variant?): Vessel = when (variant) {
            Variant.NAKKI -> SUBMARINE
            Variant.CONQUEROR -> DESTROYER
            Variant.TRIDENT -> ANGULAR_SUBMARINE
            Variant.BLACKSTEELE, null -> SHIP
        }

        val SHIP = Vessel(Path2D.Double().apply {
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
        }, waterline = 10.0)

        val SUBMARINE = Vessel(Path2D.Double().apply {
            // pressure hull
            moveTo(3.0, 5.2); lineTo(21.0, 5.2)
            curveTo(24.0, 5.2, 26.0, 6.4, 27.0, 7.6)
            curveTo(26.0, 8.8, 24.0, 10.0, 21.0, 10.0)
            lineTo(5.0, 10.0)
            curveTo(3.0, 10.0, 1.6, 8.8, 1.6, 7.6)
            curveTo(1.6, 6.4, 2.2, 5.2, 3.0, 5.2); closePath()
            // rudder
            moveTo(2.0, 7.0); lineTo(0.0, 5.4); lineTo(0.0, 9.8); lineTo(2.0, 8.2); closePath()
            // conning tower
            moveTo(11.0, 5.2); lineTo(11.8, 2.4); lineTo(16.2, 2.4); lineTo(17.0, 5.2); closePath()
            // periscope
            moveTo(13.4, 2.4); lineTo(13.4, 0.0); lineTo(15.0, 0.0); lineTo(15.0, 0.8); lineTo(14.2, 0.8)
            lineTo(14.2, 2.4); closePath()
        }, waterline = 4.4)

        /** Colonial destroyer: raised bow, boxy turrets, a block bridge and two slim upright funnels. */
        val DESTROYER = Vessel(Path2D.Double().apply {
            // hull with a rounded stern and a bow that sweeps up
            moveTo(0.6, 6.4); quadTo(0.0, 8.6, 1.6, 10.0); lineTo(22.6, 10.0); lineTo(26.0, 4.4)
            lineTo(20.2, 5.6); lineTo(20.2, 6.4); closePath()
            // bridge with its angled top
            moveTo(13.6, 6.4); lineTo(13.6, 2.6); lineTo(14.2, 1.8); lineTo(15.6, 1.8); lineTo(16.2, 2.6)
            lineTo(16.2, 6.4); closePath()
            // pole mast with the radar frame
            moveTo(12.4, 6.4); lineTo(12.4, 0.0); lineTo(12.9, 0.0); lineTo(12.9, 6.4); closePath()
            moveTo(11.6, 0.4); lineTo(13.7, 0.4); lineTo(13.7, 1.5); lineTo(11.6, 1.5); closePath()
            // two funnels
            moveTo(7.8, 6.4); lineTo(7.8, 2.2); lineTo(9.1, 2.2); lineTo(9.1, 6.4); closePath()
            moveTo(10.0, 6.4); lineTo(10.0, 2.2); lineTo(11.3, 2.2); lineTo(11.3, 6.4); closePath()
            // fore and aft turrets
            moveTo(17.2, 5.9); lineTo(17.7, 4.4); lineTo(19.7, 4.4); lineTo(20.2, 5.6); closePath()
            moveTo(2.8, 6.4); lineTo(3.3, 4.9); lineTo(5.5, 4.9); lineTo(6.0, 6.4); closePath()
        }, waterline = 10.0)

        /** Colonial submarine: long, slab-sided and sharp-edged, with a small box tower. */
        val ANGULAR_SUBMARINE = Vessel(Path2D.Double().apply {
            // hull
            moveTo(0.0, 7.6); lineTo(1.8, 5.4); lineTo(24.4, 5.4); lineTo(28.6, 7.6); lineTo(24.4, 10.0)
            lineTo(1.8, 10.0); closePath()
            // box tower
            moveTo(12.4, 5.4); lineTo(12.7, 2.2); lineTo(15.9, 2.2); lineTo(16.4, 5.4); closePath()
            // cargo hatch block and deck turret
            moveTo(17.4, 5.4); lineTo(17.4, 4.5); lineTo(21.2, 4.5); lineTo(21.2, 5.4); closePath()
            moveTo(22.0, 5.4); lineTo(22.3, 4.2); lineTo(23.8, 4.2); lineTo(24.1, 5.4); closePath()
            // periscopes
            moveTo(13.5, 2.2); lineTo(13.5, 0.0); lineTo(14.0, 0.0); lineTo(14.0, 2.2); closePath()
            moveTo(14.8, 2.2); lineTo(14.8, 0.7); lineTo(15.2, 0.7); lineTo(15.2, 2.2); closePath()
        }, waterline = 4.6)
    }
}
