package org.lushplugins.pluginupdater.geyser.api;

import org.geysermc.geyser.api.extension.Extension;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.platform.UpdaterAPI;

public class GeyserUpdaterAPI extends UpdaterAPI {

    public GeyserUpdaterAPI(UpdaterImpl<?> updater) {
        super(updater);
    }

    /**
     * Make a container available to be checked/downloaded
     * @param extension Extension to be added
     * @param pluginData Relevant update data
     */
    public void addExtension(Extension extension, PluginData pluginData) {
        addPlugin(extension.description().name(), pluginData);
    }

    /**
     * Stop a container from being checked/downloaded
     * @param extension Extension to be removed
     */
    public void removeExtension(Extension extension) {
        removePlugin(extension.description().name());
    }
}
