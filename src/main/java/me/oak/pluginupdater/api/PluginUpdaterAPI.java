package me.oak.pluginupdater.api;

import me.oak.pluginupdater.PluginUpdater;
import me.oak.pluginupdater.updater.PluginData;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.InputStreamReader;

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
}
