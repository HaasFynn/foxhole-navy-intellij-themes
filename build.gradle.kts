import java.util.Properties

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

val themeTasks = fileTree("palettes") { include("*.properties") }.files.sorted().map { paletteFile ->
    val palette = Properties().apply { paletteFile.inputStream().use(::load) }
        .map { (key, value) -> key.toString() to value.toString() }
        .toMap()
    val fileName = palette.getValue("fileName")

    tasks.register<Copy>("generateTheme$fileName") {
        from("src/main/templates")
        into(generatedThemes.map { it.dir("themes/$fileName") })
        rename { it.replace("WardenNavy", fileName) }
        expand(palette)
        inputs.properties(palette)
    }
}

val generateThemes by tasks.registering(Sync::class) {
    themeTasks.forEach { from(it) { into("themes") } }
    into(layout.buildDirectory.dir("generated/theme-resources"))
}

sourceSets.main {
    resources.srcDir(generateThemes)
}
