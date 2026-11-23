package io.github.haasfynn.wardennavy.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import io.github.haasfynn.wardennavy.progress.NavalProgressBar

/** Settings | Appearance & Behavior | Warden Navy. */
class WardenNavyConfigurable : BoundConfigurable("Warden Navy") {
    override fun createPanel(): DialogPanel = panel {
        group("Progress") {
            row {
                checkBox("Show progress as a vessel on the waterline")
                    .bindSelected(WardenNavySettings::navalProgressBar)
                    .comment("A ship in Blacksteele, a submarine in Nakki. Bars that are already visible keep their look.")
            }
        }
        group("Effects") {
            row {
                checkBox("Air bubbles rise from the caret when you pause typing")
                    .bindSelected(WardenNavySettings::bubbles)
            }
            row {
                checkBox("A torpedo strikes the line you delete")
                    .bindSelected(WardenNavySettings::torpedo)
            }
            row {
                checkBox("The sonar pings when a build or run finishes")
                    .bindSelected(WardenNavySettings::sonarPing)
                    .comment("Green on success, red on failure.")
            }
            row { comment("Effects only play under a Warden Navy theme and are off in Power Save Mode.") }
        }
    }

    override fun apply() {
        super.apply()
        NavalProgressBar.sync()
    }
}
