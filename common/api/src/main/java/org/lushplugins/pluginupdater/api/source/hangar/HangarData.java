package org.lushplugins.pluginupdater.api.source.hangar;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.source.SourceData;

public class HangarData extends SourceData {
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
