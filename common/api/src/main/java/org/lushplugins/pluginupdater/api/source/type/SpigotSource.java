package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Objects;

public class SpigotSource implements Source {
    public static final String NAME = "spigot";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest("%s/resources/%s/versions/latest"
            .formatted(UpdaterConstants.Endpoint.SPIGET, resourceId));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.pluginName() + "' for updates.");
        }

        JsonObject pluginJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String version = pluginJson.get("name").getAsString();

        return pluginData.latestVersionParser().parse(version);
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        String downloadUrl = "%s/resources/%s/download".formatted(
            UpdaterConstants.Endpoint.SPIGET,
            resourceId);

        return DownloadableRelease.builder()
            .pluginData(pluginData)
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

    @Override
    public int getRateLimit() {
        return 1;
    }

    /**
     * @param resourceId The Spigot Resource id
     */
    public record Data(String resourceId) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
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
