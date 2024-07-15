package org.lushplugins.pluginupdater.api.platform.modrinth;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class ModrinthData extends PlatformData {
    private static final String NAME = "modrinth";

    private final String modrinthProjectId;
    private final boolean featuredOnly;

    public ModrinthData(ConfigurationSection configurationSection) {
        super(NAME);
        this.modrinthProjectId = configurationSection.getString("modrinth-project-id");
        this.featuredOnly = configurationSection.getBoolean("featured-only");
    }

    /**
     * @param modrinthProjectId Your Modrinth Project Slug
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthData(String modrinthProjectId, boolean featuredOnly) {
        super(NAME);
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
