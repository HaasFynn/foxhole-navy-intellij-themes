#!/usr/bin/env python3
"""Writes the blueprint artwork for each theme variant to art/<Variant>-blueprint.svg.

Run scripts/render-art.sh afterwards to turn the SVG files into the PNG files the themes load.
"""
import math
import pathlib

W, H = 980, 660          # canvas
CX, CY = 520, 318        # centre of the vessel and of the sonar rings
SCALE = 1.55             # vessel drawing units -> canvas
OX, OY = 602, 385        # centre of the vessel in its own drawing units
PAD_RIGHT, PAD_BOTTOM = 64, 52
ZOOM = 0.86

# Side views, bow to the right. Coordinates are in vessel drawing units.
WARSHIP = {
    "label": "FRIGATE  ·  PROFILE, STARBOARD",
    "length": "LOA 96.4 m",
    "height": "22.8 m",
    "span": (388, 816), "top": 262, "keel": 418,
    "outline": [
        "M388,372 L402,402 H790 L816,390 L800,362 H700 V372 Z",                    # hull above water
        "M402,402 C410,416 430,418 470,418 H740 C768,418 782,412 790,402",          # hull below water
        "M520,372 V350 H680 V372 M628,350 V330 H674 V350 M640,330 V318 H662 V330",  # superstructure
        "M543,350 L547,314 H567 L565,350 M585,350 L589,314 H609 L607,350",          # funnels
        "M714,362 V350 Q714,346 720,346 H744 Q750,346 750,350 V362",                # fore turret
        "M438,372 V360 Q438,356 444,356 H468 Q474,356 474,360 V372",                # aft turret
        "M750,352 L792,346 M438,362 L396,356",                                      # guns
        "M651,318 V262 M639,286 H663 M645,274 H657 M506,372 V288 M494,310 H518",    # masts
        "M392,372 V348 L404,353 L392,358",                                          # ensign staff
        "M404,408 L392,408 L390,418 L404,416 M410,412 h-10",                        # rudder and screw
    ],
    "detail": [
        "M420,384 H770 M440,393 H780",                                              # strakes
        "M540,358 h6 m8,0 h6 m8,0 h6 m8,0 h6 m8,0 h6 m8,0 h6 m8,0 h6 m8,0 h6",      # portholes
        "M636,338 h30 M547,322 h20 M589,322 h20",
        "M480,402 V418 M560,402 V418 M640,402 V418 M720,402 V418",                  # frames
    ],
}

SUBMARINE = {
    "label": "SUBMARINE  ·  PROFILE, STARBOARD",
    "length": "LOA 67.1 m",
    "height": "11.6 m",
    "span": (382, 822), "top": 306, "keel": 414,
    "outline": [
        "M398,392 C398,378 430,372 472,372 H752 C788,372 812,381 822,391 "
        "C812,401 788,412 752,412 H472 C430,412 398,406 398,392 Z",                 # pressure hull
        "M582,372 L590,342 H650 L662,372",                                          # conning tower
        "M400,386 L382,374 V410 L400,398",                                          # rudder
        "M690,372 V362 H706 V372 M706,365 L736,360",                                # deck gun
        "M612,342 V306 H620 M630,342 V318",                                         # periscopes
        "M470,366 H580 M664,366 H760",                                              # deck rail
        "M786,384 h22 M786,398 h22",                                                # torpedo tubes
        "M430,414 l-8,10 M440,414 l-8,10",                                          # dive planes
    ],
    "detail": [
        "M440,392 H800",
        "M470,372 V412 M530,372 V412 M590,372 V412 M650,372 V412 M710,372 V412 M760,373 V411",  # frames
        "M598,352 h44 M604,360 h6 m6,0 h6 m6,0 h6",
        "M480,402 h60 M560,402 h60 M640,402 h60",                                   # ballast tanks
    ],
}


def canvas_x(x):
    return CX + (x - OX) * SCALE


def canvas_y(y):
    return CY + (y - OY) * SCALE


def blueprint(vessel, line, glow):
    grid = "".join(f"M{x},0V{H}" for x in range(20, W, 40)) + "".join(f"M0,{y}H{W}" for y in range(18, H, 40))

    ticks = []
    for deg in range(0, 360, 5):
        a = math.radians(deg)
        outer = 322 if deg % 30 == 0 else 316
        ticks.append(f"M{CX + 310 * math.cos(a):.1f},{CY + 310 * math.sin(a):.1f}"
                     f"L{CX + outer * math.cos(a):.1f},{CY + outer * math.sin(a):.1f}")
    a0, a1 = math.radians(-76), math.radians(-40)
    wedge = (f"M{CX},{CY} L{CX + 310 * math.cos(a0):.1f},{CY + 310 * math.sin(a0):.1f} "
             f"A310,310 0 0 1 {CX + 310 * math.cos(a1):.1f},{CY + 310 * math.sin(a1):.1f} Z")

    left, right = (canvas_x(x) for x in vessel["span"])
    top, keel = canvas_y(vessel["top"]), canvas_y(vessel["keel"])
    water = canvas_y(402 if vessel is WARSHIP else 372)

    stations = []
    labels = []
    count = 8
    for i in range(count + 1):
        x = left + (right - left) * i / count
        stations.append(f"M{x:.1f},{top - 14:.1f}V{keel + 26:.1f}")
        labels.append(f'<text x="{x:.1f}" y="{keel + 40:.1f}" text-anchor="middle">{i * 10}</text>')

    dim_y = keel + 62
    dim_x = right + 34
    dimensions = (
        f"M{left:.1f},{dim_y}H{right:.1f} M{left:.1f},{dim_y - 7}v14 M{right:.1f},{dim_y - 7}v14 "
        f"M{left:.1f},{dim_y}l9,-4 M{left:.1f},{dim_y}l9,4 M{right:.1f},{dim_y}l-9,-4 M{right:.1f},{dim_y}l-9,4 "
        f"M{dim_x:.1f},{top:.1f}V{keel:.1f} M{dim_x - 7:.1f},{top:.1f}h14 M{dim_x - 7:.1f},{keel:.1f}h14 "
        f"M{dim_x:.1f},{top:.1f}l-4,9 M{dim_x:.1f},{top:.1f}l4,9 M{dim_x:.1f},{keel:.1f}l-4,-9 M{dim_x:.1f},{keel:.1f}l4,-9"
    )

    transform = f"translate({CX},{CY}) scale({SCALE}) translate({-OX},{-OY})"
    outline = " ".join(vessel["outline"])
    detail = " ".join(vessel["detail"])
    bx, by = W - 318, H - 78

    # The image is anchored to the corner of the IDE window, so leave room for the status bar and
    # the tool window stripe, and draw slightly smaller than the design grid.
    view_w, view_h = W + PAD_RIGHT, H + PAD_BOTTOM
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="{view_w * ZOOM:.0f}" height="{view_h * ZOOM:.0f}" viewBox="0 0 {view_w} {view_h}">
  <defs>
    <radialGradient id="fade" gradientUnits="userSpaceOnUse" cx="{W - 260}" cy="{H - 220}" r="760">
      <stop offset="0" stop-color="#fff" stop-opacity="1"/>
      <stop offset="0.6" stop-color="#fff" stop-opacity="0.8"/>
      <stop offset="1" stop-color="#fff" stop-opacity="0"/>
    </radialGradient>
    <mask id="sheet"><rect width="{W}" height="{H}" fill="url(#fade)"/></mask>
    <linearGradient id="sweep" gradientUnits="userSpaceOnUse"
                    x1="{CX + 280 * math.cos(a0):.0f}" y1="{CY + 280 * math.sin(a0):.0f}"
                    x2="{CX + 280 * math.cos(a1):.0f}" y2="{CY + 280 * math.sin(a1):.0f}">
      <stop offset="0" stop-color="{glow}" stop-opacity="0"/>
      <stop offset="1" stop-color="{glow}" stop-opacity="0.35"/>
    </linearGradient>
  </defs>
  <g mask="url(#sheet)" fill="none" stroke="{line}" stroke-linecap="round" stroke-linejoin="round"
     font-family="Menlo, monospace" font-size="10">
    <path d="{grid}" stroke-width="0.6" opacity="0.22"/>
    <path d="{wedge}" fill="url(#sweep)" stroke="none"/>
    <g stroke-width="1" opacity="0.55">
      <circle cx="{CX}" cy="{CY}" r="110"/>
      <circle cx="{CX}" cy="{CY}" r="210"/>
      <circle cx="{CX}" cy="{CY}" r="310" stroke-width="1.6"/>
      <path d="{' '.join(ticks)}"/>
    </g>
    <path d="{' '.join(stations)}" stroke-width="0.8" stroke-dasharray="2 5" opacity="0.6"/>
    <path d="M{left - 40:.1f},{water:.1f}H{right + 26:.1f}" stroke-width="0.9" stroke-dasharray="14 4 2 4" opacity="0.8"/>
    <g transform="{transform}">
      <path d="{detail}" stroke-width="{0.8 / SCALE:.2f}" opacity="0.7"/>
      <path d="{outline}" stroke-width="{1.7 / SCALE:.2f}"/>
    </g>
    <path d="{dimensions}" stroke-width="0.9"/>
    <path d="M{bx},{by}h298v58h-298z M{bx},{by + 20}h298 M{bx + 190},{by + 20}v38" stroke-width="1"/>
    <g fill="{line}" stroke="none">
      {''.join(labels)}
      <text x="{(left + right) / 2:.1f}" y="{dim_y - 6}" text-anchor="middle">{vessel["length"]}</text>
      <text x="{dim_x + 8:.1f}" y="{(top + keel) / 2:.1f}">{vessel["height"]}</text>
      <text x="{right + 30:.1f}" y="{water - 5:.1f}">W.L.</text>
      <text x="{bx + 8}" y="{by + 14}">{vessel["label"]}</text>
      <text x="{bx + 8}" y="{by + 36}">SCALE 1:400</text>
      <text x="{bx + 8}" y="{by + 50}">NAVAL WORKS · DRY DOCK 2</text>
      <text x="{bx + 198}" y="{by + 36}">SHEET 1 / 4</text>
      <text x="{bx + 198}" y="{by + 50}">REV. C</text>
    </g>
  </g>
</svg>
'''


def main():
    art = pathlib.Path(__file__).resolve().parent.parent / "art"
    art.mkdir(exist_ok=True)
    (art / "Blacksteele-blueprint.svg").write_text(blueprint(WARSHIP, "#6FA3D4", "#6FA3D4"))
    (art / "Nakki-blueprint.svg").write_text(blueprint(SUBMARINE, "#4FD1B5", "#4FD1B5"))


if __name__ == "__main__":
    main()
