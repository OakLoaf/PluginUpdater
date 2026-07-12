package org.lushplugins.pluginupdater.api.source.type;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.http.HttpResponse;

public class HangarSource implements Source {
    public static final String NAME = "hangar";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String projectSlug))) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/projects/%s/latestrelease", UpdaterConstants.Endpoint.HANGAR, projectSlug));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        String version = response.body();

        return pluginData.getLatestVersionParser().parse(version);
    }

    @Override
    public DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String projectSlug))) {
            return null;
        }

        Version version = pluginData.getLatestVersion();
        String downloadUrl = "%s/projects/%s/versions/%s/PAPER/download".formatted(
            UpdaterConstants.Endpoint.HANGAR,
            projectSlug,
            version.version());

        return new DownloadableRelease(downloadUrl, null, null);
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        return null;
    }

    @Override
    public int getRateLimit() {
        return 1;
    }

    /**
     * @param projectSlug The Hangar Project Slug
     */
    public record Data(String projectSlug) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }
    }
}
