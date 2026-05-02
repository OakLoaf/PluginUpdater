package org.lushplugins.pluginupdater.api.source.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.source.SourceData;

public class SpigotData extends SourceData {
    private static final String NAME = "spigot";

    private final String resourceId;

    public SpigotData(ConfigurationSection config) {
        super(NAME);
        resourceId = config.getString("spigot-resource-id");
    }

    /**
     * @param resourceId The Spigot Resource Id
     */
    public SpigotData(String resourceId) {
        super(NAME);
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
