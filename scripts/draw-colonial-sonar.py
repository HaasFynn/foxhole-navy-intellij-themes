#!/usr/bin/env python3
"""Writes the Colonial sonar artwork to art/Conqueror-sonar.svg and art/Trident-sonar.svg.

Centre, radius and canvas match the Warden sonar drawings, because the sonar ping effect and
the sea chart are positioned on them. The Colonial display has wedge marks around its rim
instead of fine ticks, and the vessels are all edges and flat plate.
"""
import math
import pathlib

CX, CY, RIM = 600, 390, 340


def rim_wedges(colour):
    """Small wedges pointing outward all round the rim, longer every 30 degrees."""
    wedges = []
    for degrees in range(0, 360, 10):
        tip = RIM + (24 if degrees % 30 == 0 else 13)
        half = math.radians(1.7)
        a = math.radians(degrees)
        points = [(RIM * math.cos(a - half), RIM * math.sin(a - half)),
                  (tip * math.cos(a), tip * math.sin(a)),
                  (RIM * math.cos(a + half), RIM * math.sin(a + half))]
        wedges.append("M" + " L".join(f"{CX + x:.1f},{CY + y:.1f}" for x, y in points) + " Z")
    return f'<path d="{" ".join(wedges)}" fill="{colour}" stroke="none"/>'


def sonar(ring, sweep, vessel, vessel_colour, paint):
    a0, a1 = math.radians(-78), math.radians(-38)
    wedge = (f"M{CX},{CY} L{CX + RIM * math.cos(a0):.1f},{CY + RIM * math.sin(a0):.1f} "
             f"A{RIM},{RIM} 0 0 1 {CX + RIM * math.cos(a1):.1f},{CY + RIM * math.sin(a1):.1f} Z")
    blips = "".join(f'<circle cx="{x}" cy="{y}" r="{r}"/>'
                    for x, y, r in [(742, 214, 5), (398, 276, 4), (466, 560, 4), (812, 498, 3)])
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="540" height="540" viewBox="220 10 800 800">
  <defs>
    <linearGradient id="sweep" gradientUnits="userSpaceOnUse" x1="{CX + 300 * math.cos(a0):.0f}" y1="{CY + 300 * math.sin(a0):.0f}" x2="{CX + 300 * math.cos(a1):.0f}" y2="{CY + 300 * math.sin(a1):.0f}">
      <stop offset="0" stop-color="{sweep}" stop-opacity="0"/>
      <stop offset="1" stop-color="{sweep}" stop-opacity="0.45"/>
    </linearGradient>
  </defs>
  <path d="{wedge}" fill="url(#sweep)"/>
  <g fill="none" stroke="{ring}" stroke-width="1.5">
    <circle cx="{CX}" cy="{CY}" r="110" opacity="0.55"/>
    <circle cx="{CX}" cy="{CY}" r="190" opacity="0.7"/>
    <circle cx="{CX}" cy="{CY}" r="270" opacity="0.85"/>
    <circle cx="{CX}" cy="{CY}" r="{RIM}" stroke-width="2.5"/>
    <path d="M{CX - RIM},{CY} H{CX + RIM} M{CX},{CY - RIM} V{CY + RIM}" opacity="0.45" stroke-dasharray="3 7"/>
  </g>
  {rim_wedges(ring)}
  <g fill="{sweep}">{blips}</g>
  <g fill="{vessel_colour}" stroke="{vessel_colour}" stroke-linecap="square" stroke-linejoin="miter">
{vessel.replace("PAINT", paint)}
  </g>
</svg>
'''


# Side views, bow to the right, waterline near y = 402.
DESTROYER = '''    <path d="M394,376 Q384,392 402,402 H790 L826,348 L700,366 H690 V376 Z" stroke="none"/>
    <path d="M500,376 V362 H572 V376 Z M672,366 V354 H712 V365 Z" stroke="none"/>
    <path d="M640,366 V322 L646,311 H670 L676,322 V366 Z M653,311 V304 H663 V311 Z" stroke="none"/>
    <path d="M579,376 V320 H595 V376 Z M603,376 V320 H619 V376 Z" stroke="none"/>
    <path d="M576,320 H598 M600,320 H622" stroke-width="4" fill="none"/>
    <path d="M714,364 L719,351 H749 L753,362 Z M690,354 L693,345 H708 L711,354 Z M440,376 L444,363 H474 L478,376 Z M398,376 V369 H414 V376 Z" stroke="none"/>
    <path d="M749,355 L794,349 M444,367 L402,361" stroke-width="4.5" fill="none"/>
    <path d="M708,349 L738,345" stroke-width="3" fill="none"/>
    <path d="M631,366 V268" stroke-width="3" fill="none"/>
    <path d="M620,274 H643 V294 H620 Z M620,281 H643 M620,288 H643 M628,274 V294 M636,274 V294" stroke-width="1.6" fill="none"/>
    <path d="M420,388 H786" stroke="#00000055" stroke-width="2" fill="none"/>
    <path d="M360,414 q12,-8 24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0" stroke-width="2" fill="none" opacity="0.6" stroke-linecap="round"/>'''

SUBMARINE = '''    <path d="M372,393 L394,381 H800 L834,393 L800,411 H394 Z" stroke="none"/>
    <path d="M420,381 L428,373 H772 L780,381 Z" stroke="none"/>
    <path d="M604,373 L607,344 H640 L645,373 Z" stroke="none"/>
    <path d="M660,373 V364 H730 V373 Z M738,373 L741,361 H764 L767,373 Z" stroke="none"/>
    <path d="M764,365 L800,361" stroke-width="4.5" fill="none"/>
    <path d="M617,344 V310 M627,344 V319 M635,344 V326" stroke-width="2.6" fill="none"/>
    <path d="M430,373 L621,312 L772,373" stroke-width="1.2" fill="none" opacity="0.7"/>
    <path d="M384,388 L368,381 M384,399 L368,406" stroke-width="3.5" fill="none"/>
    <path d="M684,364 V373 M707,364 V373" stroke="#00000055" stroke-width="1.6" fill="none"/>
    <path d="M436,381 L452,373 H470 L454,381 Z M748,381 L764,373 H776 L782,381 Z" fill="PAINT" stroke="none" opacity="0.75"/>
    <path d="M440,395 H796" stroke="#00000055" stroke-width="2" fill="none"/>
    <path d="M360,352 q12,-8 24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 M680,352 q12,-8 24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0" stroke-width="2" fill="none" opacity="0.6" stroke-linecap="round"/>'''


def main():
    art = pathlib.Path(__file__).resolve().parent.parent / "art"
    (art / "Conqueror-sonar.svg").write_text(sonar("#6E9A62", "#9CCB8A", DESTROYER, "#7C8C6C", "#DEE7D3"))
    (art / "Trident-sonar.svg").write_text(sonar("#2E9A68", "#6FE0A0", SUBMARINE, "#3F7F62", "#D3E6DA"))


if __name__ == "__main__":
    main()
