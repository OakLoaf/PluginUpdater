package org.lushplugins.pluginupdater.common.platform;

import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.UpdaterImpl;

public class UpdaterAPI {
    private final UpdaterImpl updater;

    public UpdaterAPI(UpdaterImpl updater) {
        this.updater = updater;
    }

    /**
     * Make a plugin available to be checked/downloaded
     * @param pluginName Name of plugin to be added
     * @param pluginData Relevant update data
     */
    public void addPlugin(String pluginName, PluginData pluginData) {
        updater.config().addPlugin(pluginName, pluginData);
    }

    /**
     * Stop a plugin from being checked/downloaded
     * @param pluginName Name of plugin to be removed
     */
    public void removePlugin(String pluginName) {
        updater.config().removePlugin(pluginName);
    }

    /**
     * Register support for a source
     * @param source Source to register
     */
    public void registerSource(Source source) {
        SourceRegistry.register(source);
    }

    /**
     * Unregister support for a source
     * @param name Name of source
     */
    public void unregisterSource(String name) {
        SourceRegistry.unregister(name);
    }
}
