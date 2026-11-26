package io.github.haasfynn.navy.progress

import java.awt.geom.Path2D

/**
 * A side view, bow to the right, on a grid [GRID_HEIGHT] units tall.
 * [waterline] is where the water surface crosses the grid: the bottom for a ship that sits on the
 * water, part way up for a submarine that runs awash.
 */
internal class Vessel(val shape: Path2D, val waterline: Double) {
    companion object {
        const val GRID_HEIGHT = 10.0

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
    }
}
