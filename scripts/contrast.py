#!/usr/bin/env python3
"""Checks the WCAG contrast of every palette in palettes/*.properties.

Exits with status 1 if any pair falls below its required ratio.
"""
import pathlib
import sys

TEXT, LARGE, SURFACE = 4.5, 3.0, 1.2

# (foreground role, background role, minimum ratio)
BODY = ["fg", "synKeyword", "synString", "synNumber", "synType", "synFunction", "synConstant",
        "synField", "synMeta", "synTag", "synAttr", "synEscape", "synOperator", "link",
        "red", "amber", "green", "cyan", "violet", "wood", "brass"]
ANSI = ["ansiRed", "ansiGreen", "ansiYellow", "ansiBlue", "ansiMagenta", "ansiCyan", "ansiWhite",
        "ansiBrightRed", "ansiBrightGreen", "ansiBrightYellow", "ansiBrightBlue",
        "ansiBrightMagenta", "ansiBrightCyan", "ansiBrightWhite"]

CHECKS = (
    [(role, "editorBg", TEXT) for role in BODY + ANSI]
    + [(role, "editorBg", LARGE) for role in ("synComment", "synDoc", "lineNumber", "ansiBrightBlack", "accent")]
    + [("fg", bg, TEXT) for bg in ("toolBg", "windowBg", "raisedBg", "hoverBg", "selectionBg",
                                   "caretRow", "searchBg", "identifierBg", "writeIdentifierBg",
                                   "diffAddBg", "diffModBg", "diffDelBg", "diffConflictBg")]
    + [("fgMuted", bg, TEXT) for bg in ("editorBg", "toolBg", "windowBg")]
    + [("lineNumberActive", "editorBg", TEXT)]
    + [("white", "accentFill", TEXT)]
    # JetBrains asks for a visible step between the window frame and the editor island.
    + [("windowBg", "editorBg", SURFACE)]
)


def luminance(hex_colour):
    def channel(value):
        value /= 255
        return value / 12.92 if value <= 0.04045 else ((value + 0.055) / 1.055) ** 2.4

    r, g, b = (channel(int(hex_colour[i:i + 2], 16)) for i in (0, 2, 4))
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(a, b):
    hi, lo = sorted((luminance(a), luminance(b)), reverse=True)
    return (hi + 0.05) / (lo + 0.05)


def load(path):
    palette = {"white": "FFFFFF"}
    for line in path.read_text().splitlines():
        if line.strip() and not line.startswith("#"):
            key, value = line.split("=", 1)
            palette[key.strip()] = value.strip()
    return palette


def main():
    root = pathlib.Path(__file__).resolve().parent.parent
    failures = 0
    for path in sorted((root / "palettes").glob("*.properties")):
        palette = load(path)
        bad = []
        for fg, bg, minimum in CHECKS:
            ratio = contrast(palette[fg], palette[bg])
            if ratio < minimum:
                bad.append(f"  {fg} on {bg}: {ratio:.2f} (needs {minimum})")
        print(f"{path.name}: {len(CHECKS) - len(bad)}/{len(CHECKS)} pairs pass")
        print("\n".join(bad), end="\n" if bad else "")
        failures += len(bad)
    return 1 if failures else 0


if __name__ == "__main__":
    sys.exit(main())
