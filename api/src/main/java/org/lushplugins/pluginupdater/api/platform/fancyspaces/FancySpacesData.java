package org.lushplugins.pluginupdater.api.platform.fancyspaces;

import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class FancySpacesData implements PlatformData {
    private final String spaceId;

    public FancySpacesData(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getSpaceId() {
        return spaceId;
    }
}
