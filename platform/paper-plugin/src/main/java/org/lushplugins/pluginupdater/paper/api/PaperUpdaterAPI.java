package org.lushplugins.pluginupdater.paper.api;

import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.platform.UpdaterAPI;
import org.lushplugins.pluginupdater.paper.PaperUpdaterPlatform;

public class PaperUpdaterAPI extends UpdaterAPI {

    public PaperUpdaterAPI(PaperUpdaterPlatform updater) {
        super(updater);
    }

    /**
     * Make a plugin available to be checked/downloaded
     * @param plugin Plugin to be added
     * @param pluginData Relevant update data
     */
    public void addPlugin(Plugin plugin, PluginData pluginData) {
        addPlugin(plugin.getName(), pluginData);
    }

    /**
     * Stop a plugin from being checked/downloaded
     * @param plugin Plugin to be removed
     */
    public void removePlugin(Plugin plugin) {
        removePlugin(plugin.getName());
    }
}
