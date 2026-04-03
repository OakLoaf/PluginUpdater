package org.lushplugins.pluginupdater.api;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.platform.PlatformRegistry;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

@SuppressWarnings("unused")
public class PluginUpdaterAPI {

    /**
     * Make a plugin available to be checked/downloaded
     * @param plugin Plugin to be added
     * @param pluginData Relevant update data
     */
    public static void addPlugin(Plugin plugin, PluginData pluginData) {
        addPlugin(plugin.getName(), pluginData);
    }

    /**
     * Make a plugin available to be checked/downloaded
     * @param pluginName Name of plugin to be added
     * @param pluginData Relevant update data
     */
    public static void addPlugin(String pluginName, PluginData pluginData) {
        PluginUpdater.getInstance().getConfigManager().addPlugin(pluginName, pluginData);
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
        PluginUpdater.getInstance().getConfigManager().removePlugin(pluginName);
    }

    /**
     * Register support for a platform
     * @param platform Name of platform
     * @param platformUpdater Constructor for updater
     * @param dataConstructor Constructor for platform data
     */
    public static void registerPlatform(String platform, Callable<VersionChecker> platformUpdater, PlatformRegistry.PlatformDataConstructor dataConstructor) {
        PlatformRegistry.register(platform, platformUpdater, dataConstructor);
    }

    /**
     * Unregister support for a platform
     * @param platform Name of platform
     */
    public static void unregisterPlatform(String platform) {
        PlatformRegistry.unregister(platform);
    }
}
