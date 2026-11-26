package io.github.haasfynn.navy;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import io.github.haasfynn.navy.effects.EffectsKt;
import io.github.haasfynn.navy.progress.NavalProgressBar;
import org.jetbrains.annotations.NotNull;

/**
 * Sets the plugin up when it is loaded without a restart and takes it down again before unloading.
 * Written in Java so that only these two methods of the listener interface are overridden.
 */
public final class NavyPluginListener implements DynamicPluginListener {
    @Override
    public void pluginLoaded(@NotNull IdeaPluginDescriptor pluginDescriptor) {
        if (isOurs(pluginDescriptor)) NavalProgressBar.INSTANCE.sync();
    }

    @Override
    public void beforePluginUnload(@NotNull IdeaPluginDescriptor pluginDescriptor, boolean isUpdate) {
        if (isOurs(pluginDescriptor)) {
            NavalProgressBar.INSTANCE.uninstall();
            EffectsKt.stopAllEffects();
        }
    }

    private static boolean isOurs(IdeaPluginDescriptor descriptor) {
        return Navy.PLUGIN_ID.equals(descriptor.getPluginId().getIdString());
    }
}
