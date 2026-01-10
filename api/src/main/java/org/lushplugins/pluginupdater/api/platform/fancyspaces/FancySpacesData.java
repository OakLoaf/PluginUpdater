package org.lushplugins.pluginupdater.api.platform.fancyspaces;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class FancySpacesData implements PlatformData {
    private static final String NAME = "fancyspaces";

    private final String spaceId;

    public FancySpacesData(ConfigurationSection configurationSection) {
        super(NAME);
        this.spaceId = configurationSection.getString("space-id");
    }

    public FancySpacesData(String spaceId) {
        super(NAME);
        this.spaceId = spaceId;
    }

    public String getSpaceId() {
        return spaceId;
    }
}