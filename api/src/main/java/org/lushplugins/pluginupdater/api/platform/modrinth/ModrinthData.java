package org.lushplugins.pluginupdater.api.platform.modrinth;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.util.Collections;
import java.util.List;

public class ModrinthData extends PlatformData {
    private static final String NAME = "modrinth";

    private final String modrinthProjectId;
    private final @Nullable List<String> versionTypes;
    private final boolean featuredOnly;

    public ModrinthData(ConfigurationSection configurationSection) {
        super(NAME);
        this.modrinthProjectId = configurationSection.getString("modrinth-project-id");

        if (configurationSection.isString("channels")) {
            this.versionTypes = Collections.singletonList(configurationSection.getString("channels", "release").toLowerCase());
        } else if (configurationSection.isList("channels")) {
            this.versionTypes = configurationSection.getStringList("channels").stream()
                .map(String::toLowerCase)
                .toList();
        } else {
            this.versionTypes = null;
        }

        this.featuredOnly = configurationSection.getBoolean("featured-only");
    }

    /**
     * @param modrinthProjectId The Modrinth project id
     * @param versionTypes Which version types to filter (Set to 'null' to not filter)
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthData(String modrinthProjectId, @Nullable List<String> versionTypes, boolean featuredOnly) {
        super(NAME);
        this.modrinthProjectId = modrinthProjectId;
        this.versionTypes = versionTypes;
        this.featuredOnly = featuredOnly;
    }

    /**
     * @param modrinthProjectId The Modrinth project id
     * @param versionType Which version type to filter (Set to 'null' to not filter)
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthData(String modrinthProjectId, @Nullable String versionType, boolean featuredOnly) {
        this(modrinthProjectId, Collections.singletonList(versionType), featuredOnly);
    }

    /**
     * @param modrinthProjectId The Modrinth project id
     * @param featuredOnly Whether to filter updates by Featured only
     */
    public ModrinthData(String modrinthProjectId, boolean featuredOnly) {
        this(modrinthProjectId, (List<String>) null, featuredOnly);
    }

    public String getModrinthProjectId() {
        return modrinthProjectId;
    }

    public boolean specifiesVersionType() {
        return this.versionTypes != null;
    }

    @ApiStatus.Internal
    public @Nullable String getVersionType() {
        return this.versionTypes != null ? this.versionTypes.get(0) : null;
    }

    public @Nullable List<String> getVersionTypes() {
        return versionTypes;
    }

    public boolean includeFeaturedOnly() {
        return featuredOnly;
    }

    public static class VersionType {
        String RELEASE = "release";
        String BETA = "beta";
        String ALPHA = "alpha";
    }
}
