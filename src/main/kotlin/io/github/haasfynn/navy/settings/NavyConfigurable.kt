package io.github.haasfynn.navy.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import io.github.haasfynn.navy.progress.NavalProgressBar

/** Settings | Appearance & Behavior | Warden & Colonial Navy. */
class NavyConfigurable : BoundConfigurable("Warden & Colonial Navy") {
    override fun createPanel(): DialogPanel = panel {
        group("Progress") {
            row {
                checkBox("Show progress as a vessel on the waterline")
                    .bindSelected(NavySettings::navalProgressBar)
                    .comment("A ship in Blacksteele, a submarine in Nakki. Bars that are already visible keep their look.")
            }
        }
        group("Effects") {
            row {
                checkBox("Air bubbles rise from the caret when you pause typing")
                    .bindSelected(NavySettings::bubbles)
            }
            row {
                checkBox("A torpedo strikes the line you delete")
                    .bindSelected(NavySettings::torpedo)
            }
            row {
                checkBox("The sonar pings when a build or run finishes")
                    .bindSelected(NavySettings::sonarPing)
                    .comment("Green on success, red on failure.")
            }
            row { comment("Effects only play under one of this plugin's themes and are off in Power Save Mode.") }
        }
    }

    override fun apply() {
        super.apply()
        NavalProgressBar.sync()
    }
}
