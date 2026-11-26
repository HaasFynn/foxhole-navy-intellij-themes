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


# Side views, bow to the right, waterline near y = 402. Proportions follow the vessels as they
# appear in the game; the drawings themselves are original.
DESTROYER = '''    <path d="M392,378 Q386,394 404,402 H806 L826,350 L590,366 L576,378 Z" stroke="none"/>
    <path d="M398,378 V371 H411 V378 Z M486,378 V373 H523 V378 Z" stroke="none"/>
    <path d="M424,378 L427,362 L433,357 H455 L459,364 L460,378 Z" stroke="none"/>
    <path d="M428,365 L397,361 M428,371 L399,368" stroke-width="3.2" fill="none"/>
    <path d="M532,378 V370 H584 V378 Z" stroke="none"/>
    <path d="M539,372 L536,328 H553 L557,372 Z M565,372 L563,328 H580 L584,372 Z" stroke="none"/>
    <path d="M534,327 H555 M561,327 H582" stroke-width="5" fill="none"/>
    <path d="M592,367 V256" stroke-width="3" fill="none"/>
    <path d="M579,286 H605 M584,274 H600 M586,262 H598 M586,262 V274 M598,262 V274" stroke-width="1.8" fill="none"/>
    <path d="M592,256 L600,259 L592,262" stroke-width="1.4" fill="none"/>
    <path d="M600,366 V331 L606,320 H644 L650,331 V365 Z M617,320 V311 H635 V320 Z M622,311 V306 H630 V311 Z" stroke="none"/>
    <path d="M650,365 V352 H690 L701,362 Z M662,352 L665,342 H684 L688,352 Z" stroke="none"/>
    <path d="M686,346 L713,343" stroke-width="3" fill="none"/>
    <path d="M716,362 L719,346 L726,339 H760 L768,347 L771,359 Z" stroke="none"/>
    <path d="M766,346 L813,340 M768,352 L814,347" stroke-width="3.4" fill="none"/>
    <path d="M816,348 V340" stroke-width="2.4" fill="none"/>
    <path d="M412,390 H800" stroke="#00000055" stroke-width="2" fill="none"/>
    <path d="M440,386 h4 m14,0 h4 m14,0 h4 m110,0 h4 m14,0 h4 m14,0 h4 m60,-6 h4 m14,0 h4" stroke="#00000066" stroke-width="3" fill="none"/>
    <circle cx="806" cy="372" r="3.4" fill="#00000066" stroke="none"/>
    <path d="M360,414 q12,-8 24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0" stroke-width="2" fill="none" opacity="0.6" stroke-linecap="round"/>'''

SUBMARINE = '''    <path d="M368,397 L402,385 H822 L838,379 L842,398 Q838,411 820,412 H400 Z" stroke="none"/>
    <path d="M452,385 L458,377 H800 L812,385 Z" stroke="none"/>
    <path d="M578,377 V357 H626 V377 Z M584,357 V341 H612 V357 Z" stroke="none"/>
    <path d="M589,341 V305 M597,341 V313" stroke-width="3.2" fill="none"/>
    <path d="M586,305 H592" stroke-width="2.2" fill="none"/>
    <path d="M626,361 L692,377 H626 Z" stroke="none"/>
    <path d="M630,357 L686,371" stroke-width="2.2" fill="none"/>
    <path d="M704,377 V365 H760 V377 Z" stroke="none"/>
    <path d="M770,377 L773,368 L812,375 L816,377 Z" stroke="none"/>
    <path d="M372,397 V372 M840,379 V362" stroke-width="1.6" fill="none"/>
    <path d="M372,373 L588,306 L840,363" stroke-width="1" fill="none" opacity="0.65"/>
    <path d="M722,365 V377 M741,365 V377" stroke="#00000055" stroke-width="1.6" fill="none"/>
    <path d="M462,377 H514 L498,384 H456 Z M552,377 H574 V384 H546 Z M776,376 L806,376 L774,370 Z" fill="PAINT" stroke="none" opacity="0.8"/>
    <path d="M750,391 H770 V397 H750 Z" fill="PAINT" stroke="none" opacity="0.55"/>
    <ellipse cx="832" cy="404" rx="7" ry="4" fill="#00000044" stroke="none"/>
    <path d="M420,397 H826" stroke="#00000055" stroke-width="2" fill="none"/>
    <path d="M384,404 L366,410 M384,408 L370,418" stroke-width="3" fill="none"/>
    <path d="M360,352 q12,-8 24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0 M680,352 q12,-8 24,0 t24,0 t24,0 t24,0 t24,0 t24,0 t24,0" stroke-width="2" fill="none" opacity="0.6" stroke-linecap="round"/>'''


def main():
    art = pathlib.Path(__file__).resolve().parent.parent / "art"
    (art / "Conqueror-sonar.svg").write_text(sonar("#6E9A62", "#9CCB8A", DESTROYER, "#7C8C6C", "#DEE7D3"))
    (art / "Trident-sonar.svg").write_text(sonar("#2E9A68", "#6FE0A0", SUBMARINE, "#3F7F62", "#D3E6DA"))


if __name__ == "__main__":
    main()
