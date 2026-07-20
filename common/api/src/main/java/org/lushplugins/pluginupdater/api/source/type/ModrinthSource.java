package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ModrinthSource implements Source {
    public static final String NAME = "modrinth";

    private final List<String> defaultLoaders;
    private final String serverVersion;

    @ApiStatus.Internal
    public ModrinthSource(List<String> defaultLoaders, @Nullable String serverVersion) {
        this.defaultLoaders = defaultLoaders;
        this.serverVersion = serverVersion;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data modrinthData)) {
            return null;
        }

        JsonObject versionJson = getLatestVersion(pluginData, modrinthData);
        String version = versionJson.get("version_number").getAsString();
        boolean supportsServerVersion = this.serverVersion == null || versionJson.get("game_versions").getAsJsonArray().contains(new JsonPrimitive(this.serverVersion));

        return pluginData.latestVersionParser().parse(version)
            .markAsPotentiallyUnsafe(!supportsServerVersion);
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data modrinthData)) {
            return null;
        }

        JsonObject versionJson = getLatestVersion(pluginData, modrinthData);
        String downloadUrl = versionJson.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();

        return DownloadableRelease.builder()
            .pluginData(pluginData)
            .downloadUrl(downloadUrl)
            .build();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (sourceData instanceof Data(String projectId, var loaders, var releaseChannels)) {
            return "https://modrinth.com/plugin/%s/changelog"
                .formatted(projectId);
        }

        return null;
    }

    private JsonArray getVersions(PluginData pluginData, Data modrinthData, @Nullable String serverVersion) throws IOException, InterruptedException {
        StringBuilder uriBuilder = new StringBuilder("%s/project/%s/version"
            .formatted(UpdaterConstants.Endpoint.MODRINTH, modrinthData.projectId()))
            .append("?loaders=").append(modrinthData.loaders().orElse(this.defaultLoaders).stream()
                .map(s -> "%22" + s + "%22")
                .collect(Collectors.joining(",", "[", "]")))
            .append("&include_changelog=false");

        if (serverVersion != null) {
            uriBuilder.append("&game_versions=").append("[%22").append(serverVersion).append("%22]");
        }

        modrinthData.releaseChannel().ifPresent((channel) -> {
            uriBuilder.append("&version_type=").append(channel);
        });

        HttpResponse<String> response = HttpUtil.sendRequest(uriBuilder.toString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (%s) whilst checking '%s' for updates."
                .formatted(response.statusCode(), pluginData.pluginName()));
        }

        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    private JsonObject getLatestVersion(PluginData pluginData, Data modrinthData) throws IOException, InterruptedException {
        JsonArray versions = getVersions(pluginData, modrinthData, this.serverVersion);
        if (!versions.isEmpty()) {
            JsonObject versionJson = versions.get(0).getAsJsonObject();

            Version version = pluginData.latestVersionParser().parse(versionJson.get("version_number").getAsString());

            VersionDifference versionDifference;
            try {
                VersionComparator comparator = pluginData.versionComparator().orElse(pluginData.sourceData().getFirst().defaultComparator());
                versionDifference = comparator.compare(pluginData.currentVersion(), version);
            } catch (InvalidVersionFormatException e) {
                throw new IllegalStateException("Failed to compare versions for '%s': %s"
                    .formatted(pluginData.pluginName(), e.getMessage()));
            }

            if (versionDifference != VersionDifference.LATEST) {
                return versionJson;
            }
        }

        versions = getVersions(pluginData, modrinthData, null);
        if (versions.isEmpty()) {
            throw new IllegalStateException("Failed to collect versions for '%s'"
                .formatted(pluginData.pluginName() ));
        }

        return versions.get(0).getAsJsonObject();
    }

    @Override
    public int getRateLimit() {
        return 1;
    }

    public record LatestVersion(JsonObject json, boolean potentiallyUnsafe) {}

    public static class ReleaseChannel {
        public static final List<String> ALL = null;
        public static final String RELEASE = "release";
        public static final String BETA = "beta";
        public static final String ALPHA = "alpha";
    }

    /**
     * @param projectId The Modrinth project id
     * @param loaders Which loaders to filter, {@code null} will include all loaders for your platform
     * @param releaseChannels Which release channels to filter, {@code null} will include all release channels
     */
    public record Data(String projectId, Optional<List<String>> loaders, Optional<List<String>> releaseChannels) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }

        @ApiStatus.Internal
        public Optional<String> releaseChannel() {
            return this.releaseChannels.map(List::getFirst);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String projectId;
            private List<String> loaders;
            private List<String> releaseChannels;

            private Builder() {}

            public Builder projectId(String projectId) {
                this.projectId = projectId;
                return this;
            }

            public Builder loaders(@Nullable List<String> loaders) {
                this.loaders = loaders;
                return this;
            }

            public Builder releaseChannels(@Nullable List<String> releaseChannels) {
                this.releaseChannels = releaseChannels;
                return this;
            }

            public Data build() {
                return new Data(
                    Objects.requireNonNull(projectId),
                    Optional.ofNullable(loaders),
                    Optional.ofNullable(releaseChannels)
                );
            }
        }
    }
}
