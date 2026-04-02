package org.lushplugins.pluginupdater.api.platform.modrinth;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.util.Collections;
import java.util.List;

public class ModrinthData extends PlatformData {
    private static final String NAME = "modrinth";

    private final String projectId;
    private final @Nullable List<String> releaseChannels;

    public ModrinthData(ConfigurationSection config) {
        super(NAME);
        this.projectId = config.getString("modrinth-project-id");

        if (config.isString("channels")) {
            this.releaseChannels = Collections.singletonList(config.getString("channels", ReleaseChannel.RELEASE).toLowerCase());
        } else if (config.isList("channels")) {
            this.releaseChannels = config.getStringList("channels").stream()
                .map(String::toLowerCase)
                .toList();
        } else {
            this.releaseChannels = ReleaseChannel.ALL;
        }
    }

    /**
     * @param projectId The Modrinth project id
     * @param releaseChannels Which release channels to filter, {@code null} will include all release channels
     */
    public ModrinthData(String projectId, @Nullable List<String> releaseChannels) {
        super(NAME);
        this.projectId = projectId;
        this.releaseChannels = releaseChannels;
    }

    /**
     * @param projectId The Modrinth project id
     * @param releaseChannel Which release channel to filter, {@code null} will include all release channels
     */
    public ModrinthData(String projectId, @Nullable String releaseChannel) {
        this(projectId, Collections.singletonList(releaseChannel));
    }

    /**
     * @param projectId The Modrinth project id
     */
    public ModrinthData(String projectId) {
        this(projectId, ReleaseChannel.ALL);
    }

    public String getProjectId() {
        return projectId;
    }

    public boolean specifiesVersionType() {
        return this.releaseChannels != null;
    }

    @ApiStatus.Internal
    public @Nullable String getVersionType() {
        return this.releaseChannels != null ? this.releaseChannels.get(0) : null;
    }

    public @Nullable List<String> getReleaseChannels() {
        return releaseChannels;
    }

    public static class ReleaseChannel {
        public static final List<String> ALL = null;
        public static final String RELEASE = "release";
        public static final String BETA = "beta";
        public static final String ALPHA = "alpha";
    }
}
