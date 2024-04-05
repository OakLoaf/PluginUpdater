package me.oak.pluginupdater.api;

import me.oak.pluginupdater.PluginUpdater;
import me.oak.pluginupdater.updater.PluginData;
import me.oak.pluginupdater.updater.VersionChecker;
import me.oak.pluginupdater.updater.platform.PlatformRegistry;
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
    public static void registerPlatform(String platform, Callable<VersionChecker> platformUpdater, PlatformRegistry.PluginDataConstructor dataConstructor) {
        PluginUpdater.getInstance().getPlatformRegistry().register(platform, platformUpdater, dataConstructor);
    }

    /**
     * Unregister support for a platform
     * @param platform Name of platform
     */
    public static void unregisterPlatform(String platform) {
        PluginUpdater.getInstance().getPlatformRegistry().unregister(platform);
    }
}
