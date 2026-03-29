package org.lushplugins.pluginupdater.api.platform.hangar;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class HangarData extends PlatformData {
    private static final String NAME = "hangar";

    private final String projectSlug;

    public HangarData(ConfigurationSection config) {
        super(NAME);
        this.projectSlug = config.getString("hangar-project-slug");
    }

    /**
     * @param projectSlug The Hangar Project Slug
     */
    public HangarData(String projectSlug) {
        super(NAME);
        this.projectSlug = projectSlug;
    }

    public String getProjectSlug() {
        return projectSlug;
    }
}
