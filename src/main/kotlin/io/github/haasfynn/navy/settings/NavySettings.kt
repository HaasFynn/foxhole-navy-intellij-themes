package io.github.haasfynn.navy.settings

import com.intellij.ide.util.PropertiesComponent
import kotlin.reflect.KProperty

/** The plugin's switches, stored with the IDE settings. Everything is on by default. */
object NavySettings {
    var navalProgressBar by Switch("navalProgressBar")
    var bubbles by Switch("bubbles")
    var torpedo by Switch("torpedo")
    var sonarPing by Switch("sonarPing")

    private class Switch(name: String) {
        private val key = "io.github.haasfynn.navy.$name"

        operator fun getValue(owner: Any?, property: KProperty<*>): Boolean =
            PropertiesComponent.getInstance().getBoolean(key, true)

        operator fun setValue(owner: Any?, property: KProperty<*>, value: Boolean) =
            PropertiesComponent.getInstance().setValue(key, value, true)
    }
}
