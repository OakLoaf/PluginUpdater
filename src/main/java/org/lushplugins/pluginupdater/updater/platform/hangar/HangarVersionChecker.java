package org.lushplugins.pluginupdater.updater.platform.hangar;

import com.google.common.io.CharStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.updater.PluginData;
import org.lushplugins.pluginupdater.updater.VersionChecker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HangarVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData) throws IOException {
        if (!(pluginData instanceof HangarPluginData hangarData)) {
            return null;
        }

        URL url = new URL("https://hangar.papermc.io/api/v1/projects/" + hangarData.getHangarProjectSlug() + "/latestrelease");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst getting the latest version for '" + pluginData.getPluginName() + "'.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);
        return CharStreams.toString(reader);
    }

    @Override
    public String getDownloadUrl(PluginData pluginData) {
        return pluginData instanceof HangarPluginData hangarData ? "https://hangar.papermc.io/api/v1/projects/" + hangarData.getHangarProjectSlug() + "/versions/" + hangarData.getLatestVersion() + "/PAPER/download" : null;
    }
}
