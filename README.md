# Warden Navy

Two dark IntelliJ Platform themes inspired by the Warden navy of the game Foxhole.

| Theme | Mood |
|---|---|
| **Warden Navy Blacksteele** | Gunmetal hull, wooden deck, banner blue |
| **Warden Navy Nakki** | Deep water, sonar green, periscope glass |

Fan-made. Not affiliated with or endorsed by Siege Camp. All artwork in this repository is original.

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
