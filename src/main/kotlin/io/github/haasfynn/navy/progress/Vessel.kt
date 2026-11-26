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

        /**
         * Colonial destroyer: stubby hull with a raised forecastle, large faceted turrets, an angular
         * bridge with the mast right behind it and two fat funnels.
         */
        val DESTROYER = Vessel(Path2D.Double().apply {
            // hull: rounded stern, step up to the forecastle, raked bow
            moveTo(0.4, 6.6); quadTo(0.0, 8.8, 1.4, 10.0); lineTo(24.6, 10.0); lineTo(26.0, 4.6)
            lineTo(12.2, 5.6); lineTo(11.2, 6.6); closePath()
            // aft turret
            moveTo(2.0, 6.6); lineTo(2.3, 5.2); lineTo(4.4, 5.2); lineTo(4.7, 6.6); closePath()
            // two funnels with their caps
            moveTo(8.4, 6.6); lineTo(8.2, 2.6); lineTo(8.0, 2.6); lineTo(8.0, 2.1); lineTo(9.9, 2.1)
            lineTo(9.9, 2.6); lineTo(9.7, 2.6); lineTo(9.9, 6.6); closePath()
            moveTo(10.2, 6.4); lineTo(10.0, 2.6); lineTo(9.8, 2.6); lineTo(9.8, 2.1); lineTo(11.7, 2.1)
            lineTo(11.7, 2.6); lineTo(11.5, 2.6); lineTo(11.7, 6.0); closePath()
            // mast
            moveTo(12.2, 5.6); lineTo(12.2, 0.0); lineTo(12.7, 0.0); lineTo(12.7, 5.6); closePath()
            moveTo(11.6, 1.0); lineTo(13.3, 1.0); lineTo(13.3, 1.4); lineTo(11.6, 1.4); closePath()
            // bridge with the director on top
            moveTo(13.0, 5.6); lineTo(13.0, 2.9); lineTo(13.5, 2.1); lineTo(15.7, 2.1); lineTo(16.2, 2.9)
            lineTo(16.2, 5.4); closePath()
            moveTo(14.1, 2.1); lineTo(14.1, 1.4); lineTo(15.1, 1.4); lineTo(15.1, 2.1); closePath()
            // small turret on its platform, then the fore turret
            moveTo(16.2, 5.4); lineTo(16.2, 4.3); lineTo(16.8, 4.3); lineTo(17.0, 3.6); lineTo(18.3, 3.6)
            lineTo(18.5, 4.3); lineTo(19.2, 5.2); closePath()
            moveTo(20.0, 5.1); lineTo(20.3, 3.5); lineTo(22.6, 3.5); lineTo(23.2, 4.9); closePath()
        }, waterline = 10.0)

        /**
         * Colonial submarine: long and slab-sided with a pointed stern and a blunt bow, a stepped
         * box tower amidships, a ramp falling away forward of it and a cargo hatch block.
         */
        val ANGULAR_SUBMARINE = Vessel(Path2D.Double().apply {
            // hull
            moveTo(0.0, 7.4); lineTo(2.4, 5.8); lineTo(27.4, 5.8); lineTo(28.4, 5.3); lineTo(28.8, 7.4)
            quadTo(28.4, 10.0, 27.0, 10.0); lineTo(2.2, 10.0); closePath()
            // raised casing
            moveTo(5.0, 5.8); lineTo(5.4, 5.2); lineTo(26.2, 5.2); lineTo(27.0, 5.8); closePath()
            // stepped tower
            moveTo(12.6, 5.2); lineTo(12.6, 3.6); lineTo(15.6, 3.6); lineTo(15.6, 5.2); closePath()
            moveTo(13.0, 3.6); lineTo(13.0, 2.3); lineTo(14.8, 2.3); lineTo(14.8, 3.6); closePath()
            // periscopes
            moveTo(13.2, 2.3); lineTo(13.2, 0.0); lineTo(13.6, 0.0); lineTo(13.6, 2.3); closePath()
            moveTo(13.9, 2.3); lineTo(13.9, 0.6); lineTo(14.2, 0.6); lineTo(14.2, 2.3); closePath()
            // ramp forward of the tower
            moveTo(15.6, 4.0); lineTo(19.6, 5.2); lineTo(15.6, 5.2); closePath()
            // cargo hatch block and the bow bulwark
            moveTo(20.4, 5.2); lineTo(20.4, 4.4); lineTo(23.8, 4.4); lineTo(23.8, 5.2); closePath()
            moveTo(24.4, 5.2); lineTo(24.6, 4.6); lineTo(26.8, 5.1); lineTo(27.0, 5.2); closePath()
        }, waterline = 4.8)
    }
}
