package me.oak.pluginupdater.updater.platform.spigot;

import me.oak.pluginupdater.updater.PluginData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SpigotPluginData extends PluginData {
    private final String spigotResourceId;

    public SpigotPluginData(@NotNull Plugin plugin, ConfigurationSection configurationSection) {
        super (plugin, "spigot", configurationSection);
        spigotResourceId = configurationSection.getString("spigot-resource-id");
    }

    /**
     * @param pluginName Name of the plugin
     * @param currentVersion Current Version of the plugin (Format: 0.0.0)
     * @param spigotResourceId Your Spigot Resource Id
     */
    public SpigotPluginData(String pluginName, String currentVersion, String spigotResourceId) {
        super(pluginName, "spigot", currentVersion);
        this.spigotResourceId = spigotResourceId;
    }

    public String getSpigotResourceId() {
        return spigotResourceId;
    }
}
