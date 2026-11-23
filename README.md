# Warden Navy

Two dark IntelliJ Platform themes inspired by the Warden navy of the game Foxhole.

| Theme | Mood |
|---|---|
| **Warden Navy Blacksteele** | Gunmetal hull, wooden deck, banner blue |
| **Warden Navy Nakki** | Deep water, sonar green, periscope glass |

Fan-made. Not affiliated with or endorsed by Siege Camp. All artwork in this repository is original.

## What is themed

- **IDE chrome**: every component, recoloured through the colour ramps of the parent theme *Islands Dark*.
- **Editor colour scheme**: syntax, inspections, diff, VCS, debugger, console and terminal colours.
- **Background artwork**: a sonar display with a warship or submarine, faint behind the code and stronger in the empty frame.
- **Icons**: 45 nautical icons. Breakpoints are sea mines, folders are crates, tool windows get a sea chart, logbook, ship's bell, periscope and more. Platform icons take the palette colours.
- **Progress bar**: a ship steaming along a waterline; for indeterminate progress it crosses the bar again and again.
- **Air bubbles**: every half minute or so a breath of small bubbles rises in the focused editor and bursts. Between breaths nothing is animated.

Progress bar and bubbles can be switched off under **Settings | Appearance & Behavior | Warden Navy**.

## Build

Requires JDK 25.

```sh
./gradlew buildPlugin   # ZIP in build/distributions
./gradlew runIde        # sandbox IDE with the plugin installed
./gradlew verifyPlugin  # JetBrains Plugin Verifier
```

To preview the bubbles without waiting, start the sandbox with
`IDE_JVM_ARGS="-Dwardennavy.bubbles.demo=true" ./gradlew runIde`.

Install the ZIP via **Settings | Plugins | ⚙ | Install Plugin from Disk…**, then pick a theme under **Settings | Appearance & Behavior | Appearance**.

## How the themes are generated

Colours live in `palettes/<variant>.properties`. The build expands every palette through the templates in `src/main/templates` into one UI theme (`*.theme.json`) and one editor colour scheme (`*.xml`). To add a variant, add a palette file and a `themeProvider` entry in `src/main/resources/META-INF/plugin.xml`.

## Artwork

The background images are drawn as SVG in `art/` and rendered to `src/main/resources/images`:

```sh
scripts/render-art.sh   # needs rsvg-convert (librsvg)
```

How strongly the artwork shows is set by `transparency` under `background` (behind the editor) and `emptyFrameBackground` in `src/main/templates/WardenNavy.theme.json`.

The icons are drawn by `python3 scripts/draw-icons.py`, which also writes `src/main/icons.json`, the list of platform icons they replace. Add `--overview` to refresh the sheet below.

![Icon set](docs/icons.png)

`scripts/contrast.py` checks every palette against WCAG contrast ratios.
