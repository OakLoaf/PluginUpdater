package org.lushplugins.pluginupdater.paper.api;

import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

@SuppressWarnings("unused")
public class PluginUpdaterAPI {

    /**
     * Make a plugin available to be checked/downloaded
     * @param pluginName Name of plugin to be added
     * @param pluginData Relevant update data
     */
    public static void addPlugin(String pluginName, PluginData pluginData) {
        PluginUpdater.getInstance().getUpdater().getConfig().addPlugin(pluginName, pluginData);
    }

    /**
     * Make a plugin available to be checked/downloaded
     * @param plugin Plugin to be added
     * @param pluginData Relevant update data
     */
    public static void addPlugin(Plugin plugin, PluginData pluginData) {
        addPlugin(plugin.getName(), pluginData);
    }

    /**
     * Stop a plugin from being checked/downloaded
     * @param plugin Plugin to be removed
     */
    public static void removePlugin(Plugin plugin) {
        removePlugin(plugin.getName());
    }

    /**
     * Stop a plugin from being checked/downloaded
     * @param pluginName Name of plugin to be removed
     */
    public static void removePlugin(String pluginName) {
        PluginUpdater.getInstance().getUpdater().getConfig().removePlugin(pluginName);
    }

    /**
     * Register support for a source
     * @param name Name of source
     * @param source Source to register
     */
    public static void registerSource(String name, Source source) {
        SourceRegistry.register(name, source);
    }

    /**
     * Unregister support for a source
     * @param name Name of source
     */
    public static void unregisterSource(String name) {
        SourceRegistry.unregister(name);
    }
}
