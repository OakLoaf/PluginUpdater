package me.oak.pluginupdater.updater.platform.spigot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.oak.pluginupdater.PluginUpdater;
import me.oak.pluginupdater.updater.PluginData;
import me.oak.pluginupdater.updater.VersionChecker;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpigotVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData) throws IOException {
        if (!(pluginData instanceof SpigotPluginData spigotData)) {
            return null;
        }

        URL url = new URL("https://api.spiget.org/v2/resources/" + spigotData.getSpigotResourceId() + "/versions/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonObject pluginJson = JsonParser.parseReader(reader).getAsJsonObject();
        return pluginJson.get("name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData) {
        return pluginData instanceof SpigotPluginData spigotData ? "https://api.spiget.org/v2/resources/" + spigotData.getSpigotResourceId() + "/download" : null;
    }
}
