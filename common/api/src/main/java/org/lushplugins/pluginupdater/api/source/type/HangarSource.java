package org.lushplugins.pluginupdater.api.source.type;

import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class HangarSource implements Source {

    @Override
    public String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String projectSlug))) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/projects/%s/latestrelease", UpdaterConstants.Endpoint.HANGAR, projectSlug));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        return response.body();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, SourceData sourceData) {
        return sourceData instanceof Data(String projectSlug) ?
            "%s/projects/%s/versions/%s/PAPER/download".formatted(
                UpdaterConstants.Endpoint.HANGAR,
                projectSlug,
                pluginData.getLatestVersion()) :
            null;
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
        public String name() {
            return "hangar";
        }
    }
}
