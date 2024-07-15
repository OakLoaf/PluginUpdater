package org.lushplugins.pluginupdater.api.platform.hangar;

import com.google.common.io.CharStreams;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HangarVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException {
        if (!(platformData instanceof HangarData hangarData)) {
            return null;
        }

        URL url = new URL("https://hangar.papermc.io/api/v1/projects/" + hangarData.getHangarProjectSlug() + "/latestrelease");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + UpdaterConstants.VERSION);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst getting the latest version for '" + pluginData.getPluginName() + "'.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        return CharStreams.toString(reader);
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) {
        return platformData instanceof HangarData hangarData ? "https://hangar.papermc.io/api/v1/projects/" + hangarData.getHangarProjectSlug() + "/versions/" + pluginData.getLatestVersion() + "/PAPER/download" : null;
    }
}
