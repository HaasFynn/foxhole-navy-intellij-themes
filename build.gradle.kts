import java.util.Properties
import kotlin.math.roundToInt

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.4.21"
    id("org.jetbrains.intellij.platform") version "2.20.1"
}

group = "io.github.haasfynn"
version = "0.1.0"

kotlin {
    jvmToolchain(25)
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2026.2.3")
        pluginVerifier()
    }
}

intellijPlatform {
    buildSearchableOptions = false
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262"
            untilBuild = provider { null }
        }
    }
    pluginVerification {
        ides {
            recommended()
        }
    }
}

// Every palettes/<variant>.properties file is expanded through the templates in
// src/main/templates into one UI theme and one editor colour scheme.
val generatedThemes = layout.buildDirectory.dir("generated/themes")

fun channels(hex: String) = listOf(0, 2, 4).map { hex.substring(it, it + 2).toInt(16) }

/** Linear blend from [a] to [b]; colours are RRGGBB strings. */
fun mix(a: String, b: String, t: Double) =
    channels(a).zip(channels(b)).joinToString("") { (x, y) -> "%02X".format((x + (y - x) * t).roundToInt()) }

/** Fills every step by interpolating between the nearest anchors. */
fun ramp(prefix: String, steps: Iterable<Int>, anchors: Map<Int, String>): Map<String, String> {
    val stops = anchors.keys.sorted()
    return steps.associate { step ->
        val lo = stops.last { it <= step }
        val hi = stops.first { it >= step }
        val colour = if (lo == hi) anchors.getValue(lo)
        else mix(anchors.getValue(lo), anchors.getValue(hi), (step - lo).toDouble() / (hi - lo))
        "$prefix$step" to colour
    }
}

/**
 * The parent theme "Islands Dark" styles the whole IDE from a handful of colour ramps.
 * Redefining those ramps from the palette recolours every component consistently.
 */
fun rampColours(p: Map<String, String>): Map<String, String> {
    val bg = p.getValue("editorBg")
    val fill = p.getValue("accentFill")
    val bright = mix(p.getValue("fg"), "FFFFFF", 0.7)
    val pale = mix(p.getValue("link"), "FFFFFF", 0.85)
    val tens = (10..160 step 10)
    return ramp("gray-", tens, mapOf(
        10 to bg, 20 to p.getValue("raisedBg"), 30 to p.getValue("windowBg"), 40 to p.getValue("border"),
        70 to p.getValue("fgDisabled"), 100 to p.getValue("fgMuted"), 130 to p.getValue("fg"), 160 to bright,
    )) + ramp("blue-", tens, mapOf(
        10 to mix(bg, fill, 0.10), 40 to mix(bg, fill, 0.45), 80 to fill,
        100 to p.getValue("accent"), 120 to p.getValue("link"), 160 to pale,
    )) + ramp("Gray", 1..14, mapOf(
        1 to bg, 2 to p.getValue("windowBg"), 3 to p.getValue("border"), 7 to p.getValue("fgDisabled"),
        9 to p.getValue("fgMuted"), 12 to p.getValue("fg"), 14 to bright,
    )) + ramp("Blue", 1..13, mapOf(
        1 to mix(bg, fill, 0.30), 6 to fill, 8 to p.getValue("accent"), 10 to p.getValue("link"), 13 to pale,
    ))
}

/** Tints that are blends of two palette colours, so they follow the palette automatically. */
fun derivedColours(p: Map<String, String>): Map<String, String> {
    val bg = p.getValue("editorBg")
    fun tint(name: String, amount: Double) = mix(bg, p.getValue(name), amount)
    return mapOf(
        "tabSelectedBg" to tint("accentFill", 0.22),
        "currentBranchBg" to tint("accentFill", 0.18),
        "searchMatchBg" to tint("brass", 0.55),
        "fileYellow" to tint("amber", 0.12),
        "fileGreen" to tint("green", 0.12),
        "fileOrange" to tint("wood", 0.16),
        "fileRose" to tint("red", 0.14),
        "fileViolet" to tint("violet", 0.14),
        "fileBlue" to tint("accent", 0.14),
        "fileGray" to tint("fgMuted", 0.14),
        "iconBlueFill" to tint("link", 0.24),
        "iconRedFill" to tint("red", 0.24),
        "iconGreenFill" to tint("green", 0.24),
        "iconYellowFill" to tint("brass", 0.24),
        "iconOrangeFill" to tint("wood", 0.24),
        "iconPurpleFill" to tint("violet", 0.24),
        "breakpointBg" to tint("red", 0.24),
        "executionBg" to tint("accentFill", 0.50),
        "frameBg" to tint("accentFill", 0.24),
        "injectedBg" to tint("cyan", 0.08),
        "smartCastBg" to tint("green", 0.14),
        "textSearchBg" to tint("cyan", 0.28),
        "writeSearchBg" to tint("red", 0.30),
        "notificationBg" to tint("accentFill", 0.22),
        "errorHintBg" to tint("red", 0.20),
        "coverageFull" to tint("green", 0.45),
        "coveragePartial" to tint("amber", 0.45),
        "coverageNone" to tint("red", 0.45),
        "whitespaceModified" to tint("wood", 0.30),
        "docGuide" to tint("synDoc", 0.45),
        "annotation1" to tint("accentFill", 0.55),
        "annotation2" to tint("accentFill", 0.42),
        "annotation3" to tint("accentFill", 0.30),
        "annotation4" to tint("accentFill", 0.20),
        "annotation5" to tint("accentFill", 0.10),
    )
}

// Written by scripts/draw-icons.py: which platform icons the nautical icons replace.
val iconJson = file("src/main/icons.json").readText().trim().removePrefix("{").removeSuffix("}").trim()

val themeTasks = fileTree("palettes") { include("*.properties") }.files.sorted().map { paletteFile ->
    val palette = Properties().apply { paletteFile.inputStream().use(::load) }
        .map { (key, value) -> key.toString() to value.toString() }
        .toMap()
    val fileName = palette.getValue("fileName")
    val rampJson = rampColours(palette).entries.joinToString(",\n    ") { (name, colour) -> "\"$name\": \"#$colour\"" }
    val tokens = palette + derivedColours(palette) + ("rampColours" to rampJson) + ("iconReplacements" to iconJson)

    tasks.register<Copy>("generateTheme$fileName") {
        from("src/main/templates")
        into(generatedThemes.map { it.dir("themes/$fileName") })
        rename { it.replace("WardenNavy", fileName) }
        expand(tokens)
        inputs.properties(tokens)
    }
}

val generateThemes = tasks.register<Sync>("generateThemes") {
    themeTasks.forEach { from(it) { into("themes") } }
    into(layout.buildDirectory.dir("generated/theme-resources"))
}

sourceSets.main {
    resources.srcDir(generateThemes)
}

// Extra JVM options for the sandbox IDE, e.g. IDE_JVM_ARGS="-Dwardennavy.bubbles.demo=true" ./gradlew runIde
tasks.runIde {
    jvmArgumentProviders += CommandLineArgumentProvider {
        (System.getenv("IDE_JVM_ARGS") ?: "").split(" ").filter { it.isNotBlank() }
    }
}
