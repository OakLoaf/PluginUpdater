package org.lushplugins.pluginupdater.api.source.hangar;

import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class HangarVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof HangarData hangarData)) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/projects/%s/latestrelease", UpdaterConstants.Endpoint.HANGAR, hangarData.getProjectSlug()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        return response.body();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, SourceData sourceData) {
        return sourceData instanceof HangarData hangarData ?
            String.format("%s/projects/%s/versions/%s/PAPER/download", UpdaterConstants.Endpoint.HANGAR, hangarData.getProjectSlug(), pluginData.getLatestVersion()) :
            null;
    }
}
