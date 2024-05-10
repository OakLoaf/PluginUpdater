package org.lushplugins.pluginupdater.updater.platform.hangar;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pluginupdater.updater.PluginData;

public class HangarPluginData extends PluginData {
    private final String hangarProjectSlug;

    public HangarPluginData(@NotNull Plugin plugin, ConfigurationSection configurationSection) {
        super(plugin, "hangar", configurationSection);
        this.hangarProjectSlug = configurationSection.getString("hangar-project-slug");
    }

    /**
     * @param pluginName Name of the plugin
     * @param currentVersion Current Version of the plugin (Format: 0.0.0)
     * @param hangarProjectSlug Your Modrinth Project Slug
     */
    public HangarPluginData(String pluginName, String currentVersion, String hangarProjectSlug) {
        super(pluginName, "hangar", currentVersion);
        this.hangarProjectSlug = hangarProjectSlug;
    }

    public String getHangarProjectSlug() {
        return hangarProjectSlug;
    }
}
