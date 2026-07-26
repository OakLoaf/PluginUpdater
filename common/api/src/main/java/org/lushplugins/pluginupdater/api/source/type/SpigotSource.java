package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.http.Endpoint;
import org.lushplugins.pluginupdater.api.http.RateLimit;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;

public class SpigotSource implements Source {
    public static final String NAME = "spigot";
    public static final Endpoint ENDPOINT = Endpoint.create("https://api.spiget.org/v2", new RateLimit(300, Duration.ofMinutes(1)));

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public RateLimit getRateLimit() {
        // The spiget endpoint has no rate limit but to be reasonable we have implemented one locally
        return ENDPOINT.rateLimit();
    }

    public boolean isPremium(PluginData pluginData, String resourceId) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpUtil.sendRequest("%s/resources/%s"
            .formatted(ENDPOINT.url(), resourceId));
        HttpUtil.validateResponse(ENDPOINT, pluginData, response);

        return JsonParser.parseString(response.body()).getAsJsonObject()
            .get("premium")
            .getAsBoolean();
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        sourceData.endpoint().markRequest();
        HttpResponse<String> response = HttpUtil.sendRequest("%s/resources/%s/versions/latest"
            .formatted(sourceData.endpoint().url(), resourceId));
        HttpUtil.validateResponse(sourceData.endpoint(), pluginData, response);

        JsonObject pluginJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String rawVersion = pluginJson.get("name").getAsString();
        Version version = pluginData.latestVersionParser().parse(rawVersion);

        if (isPremium(pluginData, resourceId)) {
            version.warningTag("This is a premium Spigot resource so must be downloaded manually");
        }

        return version;
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        String downloadUrl = "%s/resources/%s/download".formatted(
            sourceData.endpoint().url(),
            resourceId);

        return DownloadableRelease.builder()
            .pluginData(pluginData)
            .endpoint(sourceData.endpoint())
            .downloadUrl(downloadUrl)
            .build();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (sourceData instanceof Data(String resourceId)) {
            return "https://www.spigotmc.org/resources/%s/updates"
                .formatted(resourceId);
        }

        return null;
    }

    /**
     * @param resourceId The Spigot Resource id
     */
    public record Data(String resourceId) implements SourceData {

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
            private String resourceId;

            private Builder() {}

            public Builder resourceId(String resourceId) {
                this.resourceId = resourceId;
                return this;
            }

            public Data build() {
                return new Data(Objects.requireNonNull(resourceId));
            }
        }
    }
}
