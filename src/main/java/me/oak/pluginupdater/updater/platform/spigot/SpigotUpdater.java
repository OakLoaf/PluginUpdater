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

public class SpigotUpdater implements VersionChecker {

    @Override
    public boolean hasUpdate(PluginData pluginData) throws IOException {
        if (!pluginData.isEnabled() || !(pluginData instanceof SpigotPluginData spigotData)) {
            return false;
        }

        String currentVersion = pluginData.getCurrentVersion();
        String spigotResourceId = spigotData.getSpigotResourceId();

        URL url = new URL("https://api.spiget.org/v2/resources/" + spigotResourceId + "/versions/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonObject pluginJson = JsonParser.parseReader(reader).getAsJsonObject();
        String latestVersion = pluginJson.get("name").getAsString();

        if (latestVersion.contains("-")) {
            latestVersion = latestVersion.split("-")[0];
        }

        if (latestVersion.isEmpty()) {
            throw new IllegalStateException("Latest version is invalid!");
        }

        if (!VersionChecker.isLatestVersion(currentVersion, latestVersion)) {
            pluginData.setLatestVersion(latestVersion);
            pluginData.setDownloadUrl("https://api.spiget.org/v2/resources/" + spigotResourceId + "/download");
            pluginData.setUpdateAvailable(true);
            return true;
        }

        return false;
    }
}
