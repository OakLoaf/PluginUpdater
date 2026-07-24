package org.lushplugins.pluginupdater.api.source.type;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.http.Endpoint;
import org.lushplugins.pluginupdater.api.http.RateLimit;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class HangarSource implements Source {
    public static final String NAME = "hangar";
    public static final Endpoint ENDPOINT = Endpoint.create("https://hangar.papermc.io/api/v1", new RateLimit(20, Duration.ofSeconds(5)));

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public RateLimit getRateLimit() {
        return ENDPOINT.rateLimit();
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String projectSlug))) {
            return null;
        }

        sourceData.endpoint().markRequest();
        HttpResponse<String> response = HttpUtil.sendRequest("%s/projects/%s/latestrelease"
            .formatted(sourceData.endpoint().url(), projectSlug));
        HttpUtil.validateResponse(sourceData.endpoint(), pluginData, response);

        String version = response.body();
        return pluginData.latestVersionParser().parse(version);
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String projectSlug))) {
            return null;
        }

        Version version = pluginData.latestVersion().orElseThrow();
        String downloadUrl = "%s/projects/%s/versions/%s/PAPER/download".formatted(
            sourceData.endpoint().url(),
            projectSlug,
            version.version());

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
     */
    public record Data(String projectSlug) implements SourceData {

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

            private Builder() {}

            public Builder projectSlug(String projectSlug) {
                this.projectSlug = projectSlug;
                return this;
            }

            public Data build() {
                return new Data(Objects.requireNonNull(projectSlug));
            }
        }
    }
}
