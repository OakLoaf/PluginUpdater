package org.lushplugins.pluginupdater.api.platform.modrinth;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class ModrinthData extends PlatformData {
    private static final String NAME = "modrinth";

    private final String modrinthProjectId;
    private final @Nullable EnumSet<VersionType> versionTypes;
    private final boolean featuredOnly;

    public ModrinthData(ConfigurationSection configurationSection) {
        super(NAME);
        this.modrinthProjectId = configurationSection.getString("modrinth-project-id");

        if (configurationSection.isString("channels")) {
            this.versionTypes = EnumSet.of(VersionType.valueOf(configurationSection.getString("channels", "release").toUpperCase()));
        } else if (configurationSection.isList("channels")) {
            this.versionTypes = configurationSection.getStringList("channels").stream()
                .map(channelRaw -> VersionType.valueOf(channelRaw.toUpperCase()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(VersionType.class)));
        } else {
            this.versionTypes = null;
        }

        this.featuredOnly = configurationSection.getBoolean("featured-only");
    }

    /**
     * @param modrinthProjectId The Modrinth project id
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthData(String modrinthProjectId, boolean featuredOnly) {
        this(modrinthProjectId, null, featuredOnly);
    }

    /**
     * @param modrinthProjectId The Modrinth project id
     * @param versionTypes Which version types to filter (Set to 'null' to not filter)
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthData(String modrinthProjectId, @Nullable EnumSet<VersionType> versionTypes, boolean featuredOnly) {
        super(NAME);
        this.modrinthProjectId = modrinthProjectId;
        this.versionTypes = versionTypes;
        this.featuredOnly = featuredOnly;
    }

    public String getModrinthProjectId() {
        return modrinthProjectId;
    }

    public @Nullable EnumSet<VersionType> getVersionTypes() {
        return versionTypes;
    }

    public boolean includeFeaturedOnly() {
        return featuredOnly;
    }

    public enum VersionType {
        RELEASE,
        BETA,
        ALPHA
    }
}
