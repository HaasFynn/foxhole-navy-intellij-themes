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
- **Background artwork**: a blueprint of a warship or submarine over a sonar display, faint behind the code and stronger in the empty frame.
- **Icons**: palette-coloured platform icons, a helm for Settings, an anchor for Bookmarks, pennants as tree arrows.
- **Progress bar**: a ship steaming along a waterline; indeterminate progress is a sonar pulse.
  Switch it off under **Settings | Appearance & Behavior | Warden Navy**.

## Build

Requires JDK 25.

```sh
./gradlew buildPlugin   # ZIP in build/distributions
./gradlew runIde        # sandbox IDE with the plugin installed
./gradlew verifyPlugin  # JetBrains Plugin Verifier
```

Install the ZIP via **Settings | Plugins | ⚙ | Install Plugin from Disk…**, then pick a theme under **Settings | Appearance & Behavior | Appearance**.

## How the themes are generated

Colours live in `palettes/<variant>.properties`. The build expands every palette through the templates in `src/main/templates` into one UI theme (`*.theme.json`) and one editor colour scheme (`*.xml`). To add a variant, add a palette file and a `themeProvider` entry in `src/main/resources/META-INF/plugin.xml`.

## Artwork

The background images are generated as SVG into `art/` and rendered to `src/main/resources/images`:

```sh
python3 scripts/draw-blueprints.py   # writes art/<Variant>-blueprint.svg
scripts/render-art.sh                # needs rsvg-convert (librsvg)
```

How strongly the artwork shows is set by `transparency` under `background` (behind the editor) and `emptyFrameBackground` in `src/main/templates/WardenNavy.theme.json`.

`scripts/contrast.py` checks every palette against WCAG contrast ratios.
