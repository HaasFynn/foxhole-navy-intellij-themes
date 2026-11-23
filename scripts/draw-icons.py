#!/usr/bin/env python3
"""Draws the nautical icon set.

Writes one SVG per icon (plus the `_dark` and `@20x20` variants the IDE looks for) to
src/main/resources/icons, and the list of platform icons they replace to src/main/icons.json,
which the build injects into every theme.

Icons use the colours of the platform's own dark icons, so the themes' ColorPalette recolours them.
"""
import json
import math
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
ICON_DIR = ROOT / "src/main/resources/icons"

LINE = "#CED0D6"     # default stroke
FILL = "#43454A"     # neutral fill
RED, RED_FILL = "#DB5C5C", "#402929"
GREEN, GREEN_FILL = "#57965C", "#253627"
BLUE, BLUE_FILL = "#548AF7", "#25324D"
YELLOW, YELLOW_FILL = "#F2C55C", "#3D3223"
GRAY = "#6F737A"

ROUND = 'stroke-linecap="round" stroke-linejoin="round"'


def stroke(d, colour=LINE, width=1, fill="none"):
    return f'<path d="{d}" stroke="{colour}" stroke-width="{width}" fill="{fill}" {ROUND}/>'


def filled(d, colour):
    return f'<path d="{d}" fill="{colour}"/>'


def circle(cx, cy, r, colour=LINE, fill="none", width=1):
    return f'<circle cx="{cx}" cy="{cy}" r="{r}" stroke="{colour}" stroke-width="{width}" fill="{fill}"/>'


def dot(cx, cy, r, colour=LINE):
    return f'<circle cx="{cx}" cy="{cy}" r="{r}" fill="{colour}"/>'


def polar(cx, cy, r, degrees):
    a = math.radians(degrees)
    return f"{cx + r * math.cos(a):.2f} {cy + r * math.sin(a):.2f}"


# ---------------------------------------------------------------- tool windows and general (16 px)

def helm():
    spokes = "".join(f"M{polar(8, 8, 1.4, 45 * i)}L{polar(8, 8, 6.8, 45 * i)}" for i in range(8))
    return circle(8, 8, 4.3) + circle(8, 8, 1.4) + stroke(spokes)


def anchor(colour=LINE, width=1):
    return circle(8, 3.2, 1.4, colour=colour, width=width) + stroke(
        "M8 4.6V13.2M5.4 6.6H10.6M3.4 9.4C3.4 11.8 5.5 13.2 8 13.2C10.5 13.2 12.6 11.8 12.6 9.4"
        "M2.3 10.8L3.4 9.1L4.8 10.5M13.7 10.8L12.6 9.1L11.2 10.5", colour=colour, width=width)


def buoy():
    return (stroke("M8 1.6V4.4M6.6 2.6H9.4")
            + stroke("M6.3 4.4H9.7L11.4 10.8H4.6Z", fill=FILL)
            + stroke("M5.4 7.6H10.6", colour=RED, width=1.4)
            + stroke("M1.6 12.6C2.7 11.4 3.7 11.4 4.8 12.6C5.9 13.8 6.9 13.8 8 12.6C9.1 11.4 10.1 11.4 11.2 12.6"
                     "C12.3 13.8 13.3 13.8 14.4 12.6", colour=BLUE))


def sea_chart():
    return (stroke("M2.5 4L6 2.5L10 4L13.5 2.5V12L10 13.5L6 12L2.5 13.5Z")
            + stroke("M6 2.5V12M10 4V13.5", width=0.8)
            + stroke("M3.9 9.8L5 8.6M7.2 7.2L8.6 6.6M11.3 7.4L12.2 6.4", colour=BLUE, width=0.9))


def logbook():
    return (stroke("M3.5 2.5H11.5C12.05 2.5 12.5 2.95 12.5 3.5V13.5H4.5C3.95 13.5 3.5 13.05 3.5 12.5Z")
            + stroke("M5.5 2.5V13.5M7.5 5.5H10.5M7.5 8H10.5M7.5 10.5H9.5", width=0.9))


def deck_plan():
    return (stroke("M8 1.5C10.5 4 11.5 6.4 11.5 9V14.5H4.5V9C4.5 6.4 5.5 4 8 1.5Z")
            + stroke("M4.5 9.5H11.5M4.5 12H11.5M8 12V14.5", width=0.9)
            + circle(8, 6.6, 1.1, width=0.9))


def morse_key():
    return (stroke("M2.5 4.5C2.5 3.95 2.95 3.5 3.5 3.5H12.5C13.05 3.5 13.5 3.95 13.5 4.5V11.5"
                   "C13.5 12.05 13.05 12.5 12.5 12.5H3.5C2.95 12.5 2.5 12.05 2.5 11.5Z")
            + stroke("M5 6.5H5.01M7 6.5H9.5M11.5 6.5H11.51", width=1.3)
            + stroke("M5 9.5H7.5M9.5 9.5H9.51M11.5 9.5H11.51", colour=BLUE, width=1.3))


def propeller(colour=LINE):
    blade = "M8 6.9C6.1 5.6 6.2 2.6 8 1.8C9.8 2.6 9.9 5.6 8 6.9Z"
    blades = "".join(f'<path d="{blade}" transform="rotate({a} 8 8)" stroke="{colour}" fill="none" {ROUND}/>'
                     for a in (0, 120, 240))
    return blades + circle(8, 8, 1.2, colour=colour)


def diving_helmet(colour=LINE, fill="none"):
    return (stroke("M8 2.6V1.2M6.6 1.2H9.4", colour=colour)
            + circle(8, 6.9, 4.4, colour=colour, fill=fill)
            + circle(8, 6.9, 2.2, colour=colour)
            + stroke("M8 4.7V9.1M5.8 6.9H10.2", colour=colour, width=0.8)
            + stroke("M4.6 10L2.4 14.4H13.6L11.4 10", colour=colour, fill=fill)
            + stroke("M5 12.4H11", colour=colour, width=0.9))


def signal_flag():
    return (stroke("M4 1.8V14.2")
            + stroke("M4 2.8H12.8L10.6 5.6L12.8 8.4H4Z", colour=YELLOW, fill=YELLOW_FILL))


def knot():
    return (stroke("M8 8C6.2 4.8 2.5 5.4 2.5 8C2.5 10.6 6.2 11.2 8 8C9.8 4.8 13.5 5.4 13.5 8"
                   "C13.5 10.6 9.8 11.2 8 8Z")
            + stroke("M1.6 13L4 10.6M14.4 3L12 5.4"))


def ship_bell():
    return (stroke("M5 1.8H11M8 1.8V3.2")
            + stroke("M8 3.2C5.6 3.2 4.6 5.2 4.6 8V10.4L3.4 12.2H12.6L11.4 10.4V8C11.4 5.2 10.4 3.2 8 3.2Z")
            + stroke("M8 12.2V13.4")
            + dot(8, 14.1, 0.8))


def periscope():
    return (stroke("M6.5 14.2V4.5C6.5 3.4 7.4 2.5 8.5 2.5H12.5V6H9.5V14.2")
            + stroke("M4.5 12H6.5M9.5 12H11.5", width=0.9)
            + stroke("M12.5 3.4V5.1", colour=BLUE, width=1.6))


def telegraph():
    ticks = "".join(f"M{polar(8, 7.5, 4.2, a)}L{polar(8, 7.5, 5.4, a)}" for a in (-150, -120, -90, -60, -30))
    return (circle(8, 7.5, 5.5)
            + stroke(ticks, width=0.8)
            + stroke(f"M8 7.5L{polar(8, 7.5, 3.6, -55)}", colour=YELLOW, width=1.2)
            + dot(8, 7.5, 0.9)
            + stroke("M5.5 14.5H10.5M8 13V14.5"))


def crane():
    return (stroke("M4.5 14.2V2.2M2.2 4.5H13.8M4.5 2.2L10.5 4.5M4.5 7.5L7.5 4.5M2.5 14.2H7")
            + stroke("M11.5 4.5V8.4")
            + stroke("M11.5 8.4C12.3 8.4 12.9 9 12.9 9.8C12.9 10.6 12.3 11.2 11.5 11.2C10.9 11.2 10.4 10.8 10.2 10.3",
                     colour=YELLOW))


def clipboard():
    return (stroke("M6 3.5H4.5V14H11.5V3.5H10")
            + stroke("M6 2.2H10V4.6H6Z")
            + stroke("M6.2 9.6L7.5 11L9.9 8.1", colour=GREEN, width=1.2))


def sonar():
    return (circle(8, 8, 5.8)
            + circle(8, 8, 2.6, width=0.9)
            + stroke(f"M8 8L{polar(8, 8, 5.8, -40)}", colour=GREEN, width=1.2)
            + dot(10.6, 9.9, 0.9, GREEN)
            + stroke("M8 1V2.2M8 13.8V15M1 8H2.2M13.8 8H15", width=0.9))


# ------------------------------------------------------------------------------- project tree

def crate(colour=LINE, fill=FILL):
    return (stroke("M2.5 6.5H13.5V13.5H2.5Z", colour=colour, fill=fill)
            + stroke("M2.5 6.5L4.2 3.5H11.8L13.5 6.5", colour=colour, fill=fill)
            + stroke("M2.5 9.2H13.5M8 6.5V9.2", colour=colour, width=0.9))


def container():
    return (stroke("M1.8 4.5H14.2V12.5H1.8Z", fill=FILL)
            + stroke("M4.6 6V11M7 6V11M9.4 6V11", width=0.9)
            + f'<rect x="10.9" y="8.4" width="3.3" height="4.1" fill="{BLUE}"/>')


# ------------------------------------------------------------------------------------ run bar

def run_pennant(colour=GREEN, fill=GREEN_FILL, width=1.2):
    return (stroke("M4 1.8V14.2", colour=colour, width=width)
            + stroke("M4 2.6L13.4 6.3L4 10Z", colour=colour, fill=fill, width=max(width, 1)))


# ------------------------------------------------------------------------------------- status

def status_error():
    points = " ".join(("M" if i == 0 else "L") + polar(8, 8, 7 if i % 2 == 0 else 4.9, -90 + 22.5 * i)
                      for i in range(16))
    return (filled(points + "Z", RED)
            + stroke("M8 5V8.4", colour="#FFFFFF", width=1.6) + dot(8, 10.8, 0.95, "#FFFFFF"))


def status_warning():
    return (stroke("M2.5 1.5V14.5", colour=YELLOW, width=1.2)
            + filled("M2.5 2.2H14.2L11.6 6.6L14.2 11H2.5Z", YELLOW)
            + stroke("M6.9 4.2V6.9", colour="#5E4D33", width=1.5) + dot(6.9, 8.9, 0.9, "#5E4D33"))


def status_success():
    return (stroke("M2.5 1.5V14.5", colour=GREEN, width=1.2)
            + filled("M2.5 2.2L14.6 6.9L2.5 11.6Z", GREEN)
            + stroke("M4.4 6.9L5.9 8.4L8.6 5.3", colour="#FFFFFF", width=1.3))


def status_info():
    return (filled("M5.9 14.5L6.8 7H9.2L10.1 14.5Z", BLUE)
            + filled("M6.3 3.6H9.7V6H6.3Z", BLUE)
            + filled("M6.6 3.6L8 1.6L9.4 3.6Z", BLUE)
            + stroke("M4.6 14.5H11.4", colour=BLUE, width=1.3)
            + stroke("M11.2 4.8H14.4M4.8 4.8H1.6", colour=BLUE, width=1.2)
            + stroke("M6.6 10.6H9.4", colour="#FFFFFF", width=1.1))


# ------------------------------------------------------------------------- breakpoints (14 px)

def mine(colour=RED, solid=True, body="round", horns=8):
    """A sea mine. `body` is 'round' or 'diamond'."""
    step = 360 // horns
    spikes = "".join(f"M{polar(7, 7, 4, a)}L{polar(7, 7, 6.2, a)}" for a in range(0, 360, step))
    shell = (f"M7 2.6L11.4 7L7 11.4L2.6 7Z" if body == "diamond"
             else "M7 3.2C9.1 3.2 10.8 4.9 10.8 7C10.8 9.1 9.1 10.8 7 10.8C4.9 10.8 3.2 9.1 3.2 7C3.2 4.9 4.9 3.2 7 3.2Z")
    if body == "diamond":
        spikes = "M7 0.8V2.6M7 11.4V13.2M0.8 7H2.6M11.4 7H13.2"
    return (stroke(spikes, colour=colour, width=1.3)
            + stroke(shell, colour=colour, fill=colour if solid else "none", width=1.2))


def mine_check():
    return mine() + stroke("M5.1 7.1L6.4 8.5L9 5.6", colour="#FFFFFF", width=1.2)


def mine_cross():
    return mine(solid=False) + stroke("M5.6 5.6L8.4 8.4M8.4 5.6L5.6 8.4", colour=RED, width=1.1)


def mine_bolt(solid=True):
    return mine(solid=solid) + filled("M7.9 3.9L5.3 7.5H6.9L6.1 10.1L8.7 6.5H7.1Z", "#FFFFFF" if solid else RED)


def mine_ring(solid=True):
    return mine(solid=solid) + circle(7, 7, 1.5, colour="#FFFFFF" if solid else RED, width=1.1)


def mine_dot():
    return mine() + dot(7, 7, 1.3, "#FFFFFF")


def mine_pair():
    def small(dx, dy):
        spikes = "".join(f"M{polar(dx, dy, 2.6, a)}L{polar(dx, dy, 4, a)}" for a in range(0, 360, 60))
        return stroke(spikes, colour=RED, width=1.1) + dot(dx, dy, 2.6, RED)
    return small(4.6, 9) + small(9.4, 5)


# name -> (size, drawing, platform icon paths it replaces, plugin id or None)
ICONS = {
    "helm": (16, helm(), ["general/gearPlain", "general/settings", "expui/general/settings"], None),
    "buoy": (16, buoy(), ["toolwindows/toolWindowBookmarks", "expui/toolwindows/bookmarks"], None),
    "seaChart": (16, sea_chart(), ["toolwindows/toolWindowProject", "expui/toolwindows/project"], None),
    "logbook": (16, logbook(), ["toolwindows/toolWindowCommit", "expui/toolwindows/commit"], None),
    "deckPlan": (16, deck_plan(), ["toolwindows/toolWindowStructure", "expui/toolwindows/structure"], None),
    "morseKey": (16, morse_key(), ["icons/OpenTerminal_13x13", "icons/expui/toolwindow/terminal"],
                 "org.jetbrains.plugins.terminal"),
    "propeller": (16, propeller(), ["toolwindows/toolWindowRun", "expui/toolwindows/run"], None),
    "divingHelmet": (16, diving_helmet(), ["toolwindows/toolWindowDebugger", "expui/toolwindows/debug"], None),
    "signalFlag": (16, signal_flag(), ["toolwindows/toolWindowProblems", "toolwindows/toolWindowProblemsEmpty",
                                       "expui/toolwindows/problems"], None),
    "knot": (16, knot(), ["toolwindows/toolWindowChanges", "expui/toolwindows/vcs"], None),
    "shipBell": (16, ship_bell(), ["toolwindows/notifications", "expui/toolwindows/notifications"], None),
    "periscope": (16, periscope(), ["toolwindows/toolWindowFind", "expui/toolwindows/find", "actions/find",
                                    "actions/search", "expui/general/search"], None),
    "telegraph": (16, telegraph(), ["toolwindows/toolWindowServices", "expui/toolwindows/services"], None),
    "crane": (16, crane(), ["toolwindows/toolWindowBuild", "expui/toolwindows/build"], None),
    "clipboard": (16, clipboard(), ["toolwindows/toolWindowTodo", "expui/toolwindows/todo"], None),
    "sonar": (16, sonar(), ["general/locate", "expui/general/locate"], None),

    "crate": (16, crate(), ["nodes/folder", "expui/nodes/folder"], None),
    "crateSource": (16, crate(BLUE, BLUE_FILL), ["modules/sourceRoot", "expui/nodes/sourceRoot"], None),
    "crateTest": (16, crate(GREEN, GREEN_FILL), ["modules/testRoot", "expui/nodes/testRoot"], None),
    "crateResources": (16, crate(YELLOW, YELLOW_FILL), ["modules/resourcesRoot", "expui/nodes/resourcesRoot"], None),
    "container": (16, container(), ["nodes/Module", "actions/moduleDirectory", "expui/nodes/module"], None),

    "runPennant": (16, run_pennant(), ["actions/execute", "expui/run/run"], None),
    "debugHelmet": (16, diving_helmet(GREEN, GREEN_FILL), ["actions/startDebugger", "expui/run/debug"], None),
    "stopAnchor": (16, anchor(RED, 1.4), ["actions/suspend", "expui/run/stop"], None),

    "statusError": (16, status_error(), ["general/balloonError", "general/error", "expui/status/error"], None),
    "statusWarning": (16, status_warning(), ["general/balloonWarning", "general/warning",
                                             "expui/status/warning"], None),
    "statusSuccess": (16, status_success(), ["status/success", "expui/status/success"], None),
    "statusInfo": (16, status_info(), ["general/balloonInformation", "general/information",
                                       "expui/status/info"], None),

    "mine": (14, mine(), ["debugger/db_set_breakpoint", "expui/breakpoints/breakpoint"], None),
    "mineDisabled": (14, mine(solid=False), ["debugger/db_disabled_breakpoint",
                                             "expui/breakpoints/breakpointDisabled"], None),
    "mineValid": (14, mine_check(), ["debugger/db_verified_breakpoint", "expui/breakpoints/breakpointValid"], None),
    "mineInvalid": (14, mine_cross(), ["debugger/db_invalid_breakpoint", "expui/breakpoints/breakpointInvalid"], None),
    "mineMuted": (14, mine(GRAY), ["debugger/db_muted_breakpoint", "expui/breakpoints/breakpointMuted"], None),
    "mineMutedDisabled": (14, mine(GRAY, solid=False), ["debugger/db_muted_disabled_breakpoint",
                                                        "expui/breakpoints/breakpointMutedDisabled"], None),
    "mineException": (14, mine_bolt(), ["debugger/db_exception_breakpoint",
                                        "expui/breakpoints/breakpointException"], None),
    "mineExceptionDisabled": (14, mine_bolt(solid=False), ["debugger/db_disabled_exception_breakpoint",
                                                           "expui/breakpoints/breakpointExceptionDisabled"], None),
    "mineMethod": (14, mine(body="diamond"), ["debugger/db_method_breakpoint",
                                              "expui/breakpoints/breakpointMethod"], None),
    "mineMethodDisabled": (14, mine(solid=False, body="diamond"), ["debugger/db_disabled_method_breakpoint",
                                                                   "expui/breakpoints/breakpointMethodDisabled"], None),
    "mineField": (14, mine_ring(), ["debugger/db_field_breakpoint", "expui/breakpoints/breakpointField"], None),
    "mineFieldDisabled": (14, mine_ring(solid=False), ["debugger/db_disabled_field_breakpoint",
                                                       "expui/breakpoints/breakpointFieldDisabled"], None),
    "mineDependent": (14, mine_dot(), ["debugger/db_dep_line_breakpoint",
                                       "expui/breakpoints/breakpointDependent"], None),
    "mineLogging": (14, mine(YELLOW), ["debugger/db_no_suspend_breakpoint",
                                       "expui/breakpoints/breakpointUnsuspendent"], None),
    "minePair": (14, mine_pair(), ["debugger/MultipleBreakpoints", "expui/breakpoints/multipleBreakpoints"], None),
}

# On coloured buttons (the green Run and red Stop widgets) the IDE looks for a white `_stroke` variant.
STROKE = {
    "runPennant": run_pennant("white", "none", 1.5),
    "debugHelmet": diving_helmet("white"),
    "stopAnchor": anchor("white", 1.5),
}

# Tree arrows are referenced directly by the theme (Tree.collapsedIcon and friends).
EXTRA = {
    "pennantRight": (16, f'<path d="M6 4.5L10.5 8L6 11.5Z" fill="#868A91" stroke="#868A91" stroke-linejoin="round"/>'),
    "pennantDown": (16, f'<path d="M4.5 6L8 10.5L11.5 6Z" fill="#868A91" stroke="#868A91" stroke-linejoin="round"/>'),
}


def svg(size, view, body):
    return (f'<svg width="{size}" height="{size}" viewBox="0 0 {view} {view}" fill="none" '
            f'xmlns="http://www.w3.org/2000/svg">\n{body}\n</svg>\n')


def overview(drawings):
    """Renders all icons, enlarged and at real size, to docs/icons.png (needs rsvg-convert)."""
    columns, cell = 9, 104
    rows = (len(drawings) + columns - 1) // columns
    parts = [f'<rect width="100%" height="100%" fill="#181D22"/>']
    for index, (name, (size, body)) in enumerate(drawings.items()):
        x, y = (index % columns) * cell, (index // columns) * cell
        parts.append(f'<g transform="translate({x + 20},{y + 8}) scale({64 / size})">{body}</g>')
        parts.append(f'<g transform="translate({x + 4},{y + 62})">{body}</g>')
        parts.append(f'<text x="{x + 52}" y="{y + 94}" font-family="Menlo, monospace" font-size="8" '
                     f'fill="#8C99A3" text-anchor="middle">{name}</text>')
    sheet = (f'<svg xmlns="http://www.w3.org/2000/svg" width="{columns * cell}" height="{rows * cell}">'
             + "".join(parts) + "</svg>")
    docs = ROOT / "docs"
    docs.mkdir(exist_ok=True)
    subprocess.run(["rsvg-convert", "--zoom", "2", "-o", str(docs / "icons.png")], input=sheet.encode(), check=True)


def main():
    ICON_DIR.mkdir(parents=True, exist_ok=True)
    for old in ICON_DIR.glob("*.svg"):
        old.unlink()

    replacements = {}
    drawings = {name: (size, body) for name, (size, body, _, _) in ICONS.items()} | EXTRA
    for name, (size, body) in drawings.items():
        sizes = [("", size)] + ([("@20x20", 20)] if size == 16 else [])
        for suffix, pixels in sizes:
            for dark in ("", "_dark"):
                (ICON_DIR / f"{name}{suffix}{dark}.svg").write_text(svg(pixels, size, body))

    for name, body in STROKE.items():
        (ICON_DIR / f"{name}_stroke.svg").write_text(svg(16, 16, body))

    for name, (_, _, targets, plugin) in ICONS.items():
        scope = replacements.setdefault(plugin, {}) if plugin else replacements
        for target in targets:
            scope[f"/{target}.svg"] = f"/icons/{name}.svg"

    (ROOT / "src/main/icons.json").write_text(json.dumps(replacements, indent=2) + "\n")
    if "--overview" in sys.argv:
        overview(drawings)
    print(f"{len(drawings)} icons, {sum(len(t) for _, _, t, _ in ICONS.values())} replacements")


if __name__ == "__main__":
    main()
