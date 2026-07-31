package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.http.Endpoint;
import org.lushplugins.pluginupdater.api.http.RateLimit;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class GeyserSource implements Source {
    public static final String NAME = "geyser";
    // The geyser endpoint has no published rate limit so we have opted for 120 requests per minute
    public static final Endpoint ENDPOINT = Endpoint.create("https://download.geysermc.org/v2", new RateLimit(120, Duration.ofMinutes(1)));

    private final String platform;

    public GeyserSource(String platform) {
        this.platform = platform;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof GeyserSource.Data(String projectName))) {
            return null;
        }

        sourceData.endpoint().markRequest();
        HttpResponse<String> response = HttpUtil.sendRequest("%s/projects/%s/versions/latest/builds/latest"
            .formatted(sourceData.endpoint().url(), projectName));
        HttpUtil.validateResponse(sourceData.endpoint(), pluginData, response);

        JsonObject releaseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String version = releaseJson.get("version").getAsString();
        int buildNum = releaseJson.get("build").getAsInt();

        return pluginData.latestVersionParser().parse(version)
            .withRawVersionString("%s (b%s)".formatted(version, buildNum))
            .withBuildNum(buildNum);
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String projectName))) {
            return null;
        }

        Version version = pluginData.latestVersion().orElseThrow();
        String downloadUrl = "%s/projects/%s/versions/%s/builds/%s/downloads/%s".formatted(
            sourceData.endpoint().url(),
            projectName,
            version.version().orElseThrow(),
            version.buildNum().orElseThrow(),
            this.platform);

        return DownloadableRelease.builder()
            .pluginData(pluginData)
            .endpoint(sourceData.endpoint())
            .downloadUrl(downloadUrl)
            .build();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (sourceData instanceof Data(String projectName)) {
            return "https://geysermc.org/download/?project=%s"
                .formatted(projectName);
        }

        return null;
    }

    /**
     * @param projectName The Geyser Project name
     */
    public record Data(String projectName) implements SourceData {

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
            private String projectName;

            private Builder() {}

            public Builder projectName(String projectName) {
                this.projectName = projectName;
                return this;
            }

            public Data build() {
                return new Data(Objects.requireNonNull(projectName));
            }
        }
    }
}
