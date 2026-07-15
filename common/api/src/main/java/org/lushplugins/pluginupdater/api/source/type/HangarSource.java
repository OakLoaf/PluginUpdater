package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.http.Endpoint;
import org.lushplugins.pluginupdater.api.http.RateLimit;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class HangarSource implements Source {
    public static final String NAME = "hangar";
    public static final Endpoint ENDPOINT = Endpoint.create("https://hangar.papermc.io/api/v1", new RateLimit(20, Duration.ofSeconds(5)));

    private final String platform;
    private final String platformVersion;

    @ApiStatus.Internal
    public HangarSource(String platform, @Nullable String platformVersion) {
        this.platform = platform.toUpperCase();
        this.platformVersion = platformVersion;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public RateLimit getRateLimit() {
        return ENDPOINT.rateLimit();
    }

    public @Nullable JsonObject fetchLatestVersionJson(PluginData pluginData, Data sourceData, @Nullable String platformVersion) throws IOException, InterruptedException {
        StringBuilder uriBuilder = new StringBuilder("%s/projects/%s/versions?platform=%s&limit=1".formatted(
            sourceData.endpoint().url(),
            sourceData.projectSlug(),
            this.platform));

        if (platformVersion != null) {
            uriBuilder.append("&platformVersion=").append(platformVersion);
        }

        if (sourceData.channel() != null) {
            uriBuilder.append("&channel=").append(sourceData.channel());
        }

        sourceData.endpoint().markRequest();
        HttpResponse<String> response = HttpUtil.sendRequest(uriBuilder.toString());
        HttpUtil.validateResponse(sourceData.endpoint(), pluginData, response);

        JsonArray result = JsonParser.parseString(response.body()).getAsJsonObject()
            .get("result").getAsJsonArray();

        return !result.isEmpty() ? result.get(0).getAsJsonObject() : null;
    }

    public JsonObject fetchLatestVersionJson(PluginData pluginData, Data sourceData) throws IOException, InterruptedException {
        JsonObject versionJson = fetchLatestVersionJson(pluginData, sourceData, this.platformVersion);
        if (versionJson != null) {
            Version version = pluginData.latestVersionParser().parse(versionJson.get("name").getAsString());

            VersionDifference versionDifference;
            try {
                VersionComparator comparator = pluginData.versionComparator().orElse(pluginData.sourceData().getFirst().defaultComparator());
                versionDifference = comparator.compare(pluginData.currentVersion(), version);
            } catch (InvalidVersionFormatException e) {
                throw new IllegalStateException("Failed to compare versions for '%s': %s"
                    .formatted(pluginData.pluginName(), e.getMessage()));
            }

            if (!versionDifference.isLatest()) {
                return versionJson;
            }
        }

        return fetchLatestVersionJson(pluginData, sourceData, null);
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data hangarData)) {
            return null;
        }

        JsonObject releaseJson = fetchLatestVersionJson(pluginData, hangarData);
        String rawVersion = releaseJson.get("name").getAsString();
        boolean supportsServerVersion = this.platformVersion == null || releaseJson
            .get("platformDependencies").getAsJsonObject()
            .get(this.platform).getAsJsonArray()
            .contains(new JsonPrimitive(this.platformVersion));
        Version version = pluginData.latestVersionParser().parse(rawVersion);

        if (!supportsServerVersion) {
            version.warningTag("This version is marked as potentially unsafe for your server version");
        }

        return version;
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String projectSlug, String channel))) {
            return null;
        }

        Version version = pluginData.latestVersion().orElseThrow();
        String downloadUrl = "%s/projects/%s/versions/%s/%s/download".formatted(
            sourceData.endpoint().url(),
            projectSlug,
            version.rawVersionString(),
            this.platform);

        return DownloadableRelease.builder()
            .pluginData(pluginData)
            .endpoint(sourceData.endpoint())
            .downloadUrl(downloadUrl)
            .build();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        return null;
    }

    /**
     * @param projectSlug The Hangar Project Slug
     * @param channel The channel to filter by, {@code null} will include all channels
     */
    public record Data(String projectSlug, @Nullable String channel) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }

        @Override
        public Endpoint endpoint() {
            return ENDPOINT;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String projectSlug;
            private String channel;

            private Builder() {}

            public Builder projectSlug(String projectSlug) {
                this.projectSlug = projectSlug;
                return this;
            }

            public Builder channel(@Nullable String channel) {
                this.channel = channel;
                return this;
            }

            public Data build() {
                return new Data(Objects.requireNonNull(projectSlug), channel);
            }
        }
    }
}
