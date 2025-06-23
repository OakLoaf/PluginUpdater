package org.lushplugins.pluginupdater.api.platform.hangar;

import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class HangarVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof HangarData hangarData)) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/projects/%s/latestrelease", UpdaterConstants.Endpoint.HANGAR, hangarData.getHangarProjectSlug()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        return response.body();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) {
        return platformData instanceof HangarData hangarData ?
            String.format("%s/projects/%s/versions/%s/PAPER/download", UpdaterConstants.Endpoint.HANGAR, hangarData.getHangarProjectSlug(), pluginData.getLatestVersion()) :
            null;
    }
}
