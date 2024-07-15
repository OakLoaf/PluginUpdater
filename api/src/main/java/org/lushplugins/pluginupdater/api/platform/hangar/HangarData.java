package org.lushplugins.pluginupdater.api.platform.hangar;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class HangarData extends PlatformData {
    private static final String NAME = "hangar";

    private final String hangarProjectSlug;

    public HangarData(ConfigurationSection configurationSection) {
        super(NAME);
        this.hangarProjectSlug = configurationSection.getString("hangar-project-slug");
    }

    /**
     * @param hangarProjectSlug Your Modrinth Project Slug
     */
    public HangarData(String hangarProjectSlug) {
        super(NAME);
        this.hangarProjectSlug = hangarProjectSlug;
    }

    public String getHangarProjectSlug() {
        return hangarProjectSlug;
    }
}
