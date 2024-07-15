package org.lushplugins.pluginupdater.api.platform.spigot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpigotVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException {
        if (!(platformData instanceof SpigotData spigotData)) {
            return null;
        }

        URL url = new URL("https://api.spiget.org/v2/resources/" + spigotData.getSpigotResourceId() + "/versions/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + UpdaterConstants.VERSION);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonObject pluginJson = JsonParser.parseReader(reader).getAsJsonObject();
        return pluginJson.get("name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) {
        return platformData instanceof SpigotData spigotData ? "https://api.spiget.org/v2/resources/" + spigotData.getSpigotResourceId() + "/download" : null;
    }
}
