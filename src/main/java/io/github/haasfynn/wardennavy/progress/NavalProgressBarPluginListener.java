package io.github.haasfynn.wardennavy.progress;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import io.github.haasfynn.wardennavy.effects.Bubbles;
import org.jetbrains.annotations.NotNull;

/**
 * Installs the progress bar and bubble effect when the plugin is loaded without a restart and removes
 * them before unloading.
 * Written in Java so that only these two methods of the listener interface are overridden.
 */
public final class NavalProgressBarPluginListener implements DynamicPluginListener {
    private static final String PLUGIN_ID = "io.github.haasfynn.wardennavy";

    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        if (PLUGIN_ID.equals(pluginDescriptor.getPluginId().getIdString())) {
            NavalProgressBar.INSTANCE.sync();
            Bubbles.INSTANCE.sync();
        }
    }

    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        if (PLUGIN_ID.equals(pluginDescriptor.getPluginId().getIdString())) {
            NavalProgressBar.INSTANCE.uninstall();
            Bubbles.INSTANCE.shutDown();
        }
    }
}
