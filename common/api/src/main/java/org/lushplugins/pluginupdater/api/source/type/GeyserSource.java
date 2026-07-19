package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.http.HttpResponse;

public class GeyserSource implements Source {
    public static final String NAME = "geyser";

    private final String platform;

    public GeyserSource(String platform) {
        this.platform = platform;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof GeyserSource.Data(String projectName))) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest("%s/projects/%s/versions/latest/builds/latest"
            .formatted(UpdaterConstants.Endpoint.GEYSER, projectName));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.pluginName() + "' for updates.");
        }

        JsonObject releaseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String version = releaseJson.get("version").getAsString();
        int buildNum = releaseJson.get("build").getAsInt();

        return pluginData.latestVersionParser().parse(version)
            .withRawVersionString("%s (b%s)".formatted(version, buildNum))
            .withBuildNum(buildNum);
    }

    @Override
    public DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String projectName))) {
            return null;
        }

        Version version = pluginData.latestVersion().orElseThrow();
        String downloadUrl = "%s/projects/%s/versions/%s/builds/%s/downloads/%s".formatted(
            UpdaterConstants.Endpoint.GEYSER,
            projectName,
            version.version(),
            version.buildNum(),
            this.platform);

        return DownloadableRelease.builder(downloadUrl)
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

    @Override
    public int getRateLimit() {
        return 1;
    }

    /**
     * @param projectName The Geyser Project name
     */
    public record Data(String projectName) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }
    }
}
