package org.lushplugins.pluginupdater.api.platform.spigot;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class SpigotData extends PlatformData {
    private static final String NAME = "spigot";

    private final String spigotResourceId;

    public SpigotData(ConfigurationSection configurationSection) {
        super(NAME);
        spigotResourceId = configurationSection.getString("spigot-resource-id");
    }

    /**
     * @param spigotResourceId Your Spigot Resource Id
     */
    public SpigotData(String spigotResourceId) {
        super(NAME);
        this.spigotResourceId = spigotResourceId;
    }

    public String getSpigotResourceId() {
        return spigotResourceId;
    }
}
