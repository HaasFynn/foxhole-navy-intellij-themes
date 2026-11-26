#!/usr/bin/env python3
"""Renders the editor and tool window background: a sea chart with depth contours and soundings,
with the sonar artwork from art/<Variant>-sonar.svg in the bottom right corner.

The IDE anchors the image to the bottom right corner of the window and shows as much of it as
fits, so it is drawn large enough for big screens. Needs rsvg-convert (librsvg).
"""
import math
import pathlib
import random
import re
import subprocess

ROOT = pathlib.Path(__file__).resolve().parent.parent
WIDTH, HEIGHT = 2880, 1700
CELL = 10                     # grid spacing used to trace the contours
LEVELS = [-0.9, -0.6, -0.3, 0.0, 0.3, 0.6, 0.9]

# Where the sonar display sits in the corner: its centre measured from the bottom right, and its
# radius including the tick marks.
SONAR_CENTRE, SONAR_RADIUS = 283, 246

VARIANTS = {"Blacksteele": "#6FA3D4", "Nakki": "#4FD1B5", "Conqueror": "#8CBF7A", "Trident": "#6FE0A0"}


def sea_floor(seed):
    """A smooth, rolling height field: a handful of long waves laid over each other."""
    rng = random.Random(seed)
    waves = [(rng.uniform(0.0016, 0.0050), rng.uniform(0, math.tau), rng.uniform(0, math.tau), rng.uniform(0.5, 1.0))
             for _ in range(7)]

    def depth(x, y):
        return sum(weight * math.sin(frequency * (x * math.cos(angle) + y * math.sin(angle)) + phase)
                   for frequency, angle, phase, weight in waves) / 2.2

    return depth


def contours(depth):
    """Marching squares: returns the contour segments of every level as SVG path data."""
    columns, rows = WIDTH // CELL + 1, HEIGHT // CELL + 1
    grid = [[depth(c * CELL, r * CELL) for c in range(columns + 1)] for r in range(rows + 1)]
    paths = []
    for level in LEVELS:
        parts = []
        for r in range(rows):
            for c in range(columns):
                corners = [grid[r][c], grid[r][c + 1], grid[r + 1][c + 1], grid[r + 1][c]]
                points = [(c, r), (c + 1, r), (c + 1, r + 1), (c, r + 1)]
                crossings = []
                for i in range(4):
                    a, b = corners[i], corners[(i + 1) % 4]
                    if (a < level) != (b < level):
                        t = (level - a) / (b - a)
                        (x0, y0), (x1, y1) = points[i], points[(i + 1) % 4]
                        crossings.append(((x0 + (x1 - x0) * t) * CELL, (y0 + (y1 - y0) * t) * CELL))
                # Two crossings make one segment; four (a saddle) make two.
                for i in range(0, len(crossings) - 1, 2):
                    (x0, y0), (x1, y1) = crossings[i], crossings[i + 1]
                    parts.append(f"M{x0:.1f},{y0:.1f}L{x1:.1f},{y1:.1f}")
        paths.append("".join(parts))
    return paths


def soundings(depth, seed):
    """Depth figures scattered over the chart, deeper water giving larger numbers."""
    rng = random.Random(seed)
    labels = []
    for y in range(140, HEIGHT - 80, 240):
        for x in range(140, WIDTH - 80, 300):
            px, py = x + rng.uniform(-70, 70), y + rng.uniform(-60, 60)
            metres = round(8 + (1 - depth(px, py)) * 46)
            labels.append(f'<text x="{px:.0f}" y="{py:.0f}">{metres}</text>')
    return "".join(labels)


def sonar(variant):
    """The sonar drawing, placed in the bottom right corner at its own size."""
    text = (ROOT / "art" / f"{variant}-sonar.svg").read_text()
    width, height = (int(v) for v in re.search(r'width="(\d+)" height="(\d+)"', text).groups())
    view = re.search(r'viewBox="([^"]+)"', text).group(1)
    body = text[text.index(">") + 1:text.rindex("</svg>")]
    return (f'<svg x="{WIDTH - width}" y="{HEIGHT - height}" width="{width}" height="{height}" '
            f'viewBox="{view}">{body}</svg>')


def main():
    for index, (variant, colour) in enumerate(VARIANTS.items()):
        depth = sea_floor(seed=1903 + index)
        lines = "".join(
            f'<path d="{path}" opacity="{0.34 + 0.05 * level}"/>' for level, path in enumerate(contours(depth)))
        svg = (
            f'<svg xmlns="http://www.w3.org/2000/svg" width="{WIDTH}" height="{HEIGHT}" '
            f'viewBox="0 0 {WIDTH} {HEIGHT}">'
            # The chart stops at the rim of the sonar display instead of running through it.
            f'<defs><mask id="sea"><rect width="{WIDTH}" height="{HEIGHT}" fill="#fff"/>'
            f'<circle cx="{WIDTH - SONAR_CENTRE}" cy="{HEIGHT - SONAR_CENTRE}" r="{SONAR_RADIUS}" fill="#000"/></mask></defs>'
            f'<g mask="url(#sea)">'
            f'<g fill="none" stroke="{colour}" stroke-width="1" stroke-linecap="round">{lines}</g>'
            f'<g fill="{colour}" opacity="0.5" font-family="Menlo, monospace" font-size="10" '
            f'text-anchor="middle">{soundings(depth, seed=77 + index)}</g></g>'
            f'{sonar(variant)}</svg>')
        out = ROOT / "src/main/resources/images" / f"{variant}-chart.png"
        subprocess.run(["rsvg-convert", "-o", str(out)], input=svg.encode(), check=True)
        print(f"rendered {out.name} ({out.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
