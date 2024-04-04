package me.oak.pluginupdater.updater.platform.modrinth;

import me.oak.pluginupdater.updater.PluginData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ModrinthPluginData extends PluginData {
    private final String modrinthProjectSlug;
    private final boolean featuredOnly;

    public ModrinthPluginData(@NotNull Plugin plugin, ConfigurationSection configurationSection) {
        super(plugin, "modrinth", configurationSection);
        this.modrinthProjectSlug = configurationSection.getString("modrinth-project-slug");
        this.featuredOnly = configurationSection.getBoolean("featured-only");
    }

    /**
     * @param pluginName Name of the plugin
     * @param currentVersion Current Version of the plugin (Format: 0.0.0)
     * @param modrinthProjectSlug Your Modrinth Project Slug
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthPluginData(String pluginName, String currentVersion, String modrinthProjectSlug, boolean featuredOnly) {
        super(pluginName, "modrinth", currentVersion);
        this.modrinthProjectSlug = modrinthProjectSlug;
        this.featuredOnly = featuredOnly;
    }

    public String getModrinthProjectSlug() {
        return modrinthProjectSlug;
    }

    public boolean includeFeaturedOnly() {
        return featuredOnly;
    }
}
