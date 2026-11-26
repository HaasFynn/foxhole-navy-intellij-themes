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

/**
         * Warden frigate: turtleback stern, round drum turrets, one fat funnel behind a large
         * rounded bridge, two tall masts and a high bulwark at the bow.
         */
        val SHIP = Vessel(Path2D.Double().apply {
            // hull
            moveTo(0.0, 7.6); quadTo(0.8, 6.7, 2.4, 6.6); lineTo(15.4, 6.6); lineTo(16.0, 6.0)
            lineTo(23.8, 6.0); lineTo(26.0, 5.0); lineTo(24.8, 10.0); lineTo(1.0, 10.0); closePath()
            // aft drum turret
            moveTo(2.8, 6.6); lineTo(2.8, 5.7); quadTo(2.8, 5.3, 3.2, 5.3); lineTo(4.9, 5.3)
            quadTo(5.3, 5.3, 5.3, 5.7); lineTo(5.3, 6.6); closePath()
            // mainmast with its ring
            moveTo(7.2, 6.6); lineTo(7.2, 0.0); lineTo(7.6, 0.0); lineTo(7.6, 6.6); closePath()
            moveTo(6.7, 2.3); lineTo(8.1, 2.3); lineTo(8.1, 2.7); lineTo(6.7, 2.7); closePath()
            // stacked drum turrets amidships
            moveTo(10.2, 6.6); lineTo(10.2, 5.9); quadTo(10.2, 5.6, 10.5, 5.6); lineTo(10.9, 5.6)
            lineTo(10.9, 5.0); quadTo(10.9, 4.7, 11.2, 4.7); lineTo(12.6, 4.7); quadTo(12.9, 4.7, 12.9, 5.0)
            lineTo(12.9, 6.6); closePath()
            // funnel
            moveTo(14.2, 6.6); lineTo(14.2, 3.9); quadTo(14.2, 3.4, 14.7, 3.4); lineTo(15.4, 3.4)
            quadTo(15.9, 3.4, 15.9, 3.9); lineTo(15.9, 6.2); closePath()
            // foremast with its ring
            moveTo(16.3, 6.0); lineTo(16.3, 0.6); lineTo(16.7, 0.6); lineTo(16.7, 6.0); closePath()
            moveTo(15.9, 2.6); lineTo(17.1, 2.6); lineTo(17.1, 3.0); lineTo(15.9, 3.0); closePath()
            // bridge
            moveTo(16.7, 6.0); lineTo(16.7, 4.5); quadTo(16.7, 3.9, 17.3, 3.9); lineTo(19.4, 3.9)
            quadTo(20.0, 3.9, 20.0, 4.5); lineTo(20.4, 5.2); lineTo(20.4, 6.0); closePath()
            // two small fore turrets, the after one raised on its barbette
            moveTo(20.6, 6.0); lineTo(20.6, 4.6); lineTo(22.1, 4.6); lineTo(22.1, 6.0); closePath()
            moveTo(21.7, 6.0); lineTo(21.7, 5.3); lineTo(23.2, 5.3); lineTo(23.2, 6.0); closePath()
        }, waterline = 10.0)

        /**
         * Warden submarine: rounded hull, wide flat deck, open horseshoe tower forward of midships
         * with the gun tub behind it, and a narrow knife bow.
         */
        val SUBMARINE = Vessel(Path2D.Double().apply {
            // pressure hull
            moveTo(0.0, 7.4); curveTo(2.0, 6.0, 4.0, 5.6, 6.0, 5.6); lineTo(24.6, 5.6)
            curveTo(25.6, 5.6, 26.4, 5.8, 27.0, 6.2); lineTo(27.0, 9.2)
            curveTo(25.0, 9.9, 23.0, 10.0, 21.0, 10.0); lineTo(6.0, 10.0)
            curveTo(3.6, 10.0, 1.4, 9.0, 0.0, 7.4); closePath()
            // knife bow
            moveTo(26.2, 5.2); lineTo(28.0, 4.2); lineTo(28.0, 9.0); lineTo(27.0, 9.4); closePath()
            // deck
            moveTo(3.2, 5.6); lineTo(3.2, 5.1); lineTo(26.4, 5.1); lineTo(26.4, 5.6); closePath()
            // horseshoe tower
            moveTo(16.2, 5.1); lineTo(16.2, 3.2); quadTo(16.2, 2.6, 16.8, 2.6); lineTo(18.4, 2.6)
            quadTo(19.0, 2.6, 19.0, 3.2); lineTo(19.8, 5.1); closePath()
            // masts
            moveTo(18.0, 2.6); lineTo(18.0, 0.0); lineTo(18.4, 0.0); lineTo(18.4, 2.6); closePath()
            moveTo(18.7, 2.6); lineTo(18.7, 0.8); lineTo(19.0, 0.8); lineTo(19.0, 2.6); closePath()
            // gun tub
            moveTo(13.0, 5.1); lineTo(13.0, 4.3); lineTo(15.4, 4.3); lineTo(15.4, 5.1); closePath()
            // rudder
            moveTo(0.8, 8.0); lineTo(0.0, 9.6); lineTo(1.6, 9.2); closePath()
        }, waterline = 4.9)

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
