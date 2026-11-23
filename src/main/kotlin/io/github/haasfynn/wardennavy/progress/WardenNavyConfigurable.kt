package io.github.haasfynn.wardennavy.progress

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

/** Settings | Appearance & Behavior | Warden Navy. */
class WardenNavyConfigurable : BoundConfigurable("Warden Navy") {
    override fun createPanel(): DialogPanel = panel {
        row {
            checkBox("Show progress as a ship on the waterline")
                .bindSelected(NavalProgressBar::enabled)
                .comment("Only while a Warden Navy theme is active. Progress bars that are already visible keep their look.")
        }
    }

    override fun apply() {
        super.apply()
        NavalProgressBar.sync()
    }
}
