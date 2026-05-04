package org.lushplugins.pluginupdater.velocity.api;

import com.velocitypowered.api.plugin.PluginContainer;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.platform.UpdaterAPI;
import org.lushplugins.pluginupdater.velocity.VelocityUpdaterPlatform;

public class VelocityUpdaterAPI extends UpdaterAPI {

    public VelocityUpdaterAPI(VelocityUpdaterPlatform updater) {
        super(updater);
    }

    /**
     * Make a plugin available to be checked/downloaded
     * @param plugin Plugin to be added
     * @param pluginData Relevant update data
     */
    public void addPlugin(PluginContainer plugin, PluginData pluginData) {
        addPlugin(plugin.getDescription().getName().orElseThrow(), pluginData);
    }

    /**
     * Stop a plugin from being checked/downloaded
     * @param plugin Plugin to be removed
     */
    public void removePlugin(PluginContainer plugin) {
        removePlugin(plugin.getDescription().getName().orElseThrow());
    }
}
