package org.lushplugins.pluginupdater.updater.platform.modrinth;

import org.lushplugins.pluginupdater.updater.PluginData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ModrinthPluginData extends PluginData {
    private final String modrinthProjectId;
    private final boolean featuredOnly;

    public ModrinthPluginData(@NotNull Plugin plugin, ConfigurationSection configurationSection) {
        super(plugin, "modrinth", configurationSection);
        this.modrinthProjectId = configurationSection.getString("modrinth-project-id");
        this.featuredOnly = configurationSection.getBoolean("featured-only");
    }

    /**
     * @param pluginName Name of the plugin
     * @param currentVersion Current Version of the plugin (Format: 0.0.0)
     * @param modrinthProjectId Your Modrinth Project Slug
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthPluginData(String pluginName, String currentVersion, String modrinthProjectId, boolean featuredOnly) {
        super(pluginName, "modrinth", currentVersion);
        this.modrinthProjectId = modrinthProjectId;
        this.featuredOnly = featuredOnly;
    }

    public String getModrinthProjectId() {
        return modrinthProjectId;
    }

    public boolean includeFeaturedOnly() {
        return featuredOnly;
    }
}
