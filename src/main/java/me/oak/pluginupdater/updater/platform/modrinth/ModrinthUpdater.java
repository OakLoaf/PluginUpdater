package me.oak.pluginupdater.updater.platform.modrinth;

import com.google.gson.JsonArray;
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

public class ModrinthUpdater implements VersionChecker {

    @Override
    public boolean hasUpdate(PluginData pluginData) throws IOException {
        if (!pluginData.isEnabled() || !(pluginData instanceof ModrinthPluginData modrinthData)) {
            return false;
        }

        String currentVersion = pluginData.getCurrentVersion();
        String modrinthProjectSlug = modrinthData.getModrinthProjectSlug();

        URL url = new URL("https://api.modrinth.com/v2/project/" + modrinthProjectSlug + "/version" + (modrinthData.includeFeaturedOnly() ? "?featured=true" : ""));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonArray versionsJson = JsonParser.parseReader(reader).getAsJsonArray();
        JsonObject currVersionJson = versionsJson.get(0).getAsJsonObject();
        String latestVersion = currVersionJson.get("version_number").getAsString();

        if (latestVersion.contains("-")) {
            latestVersion = latestVersion.split("-")[0];
        }

        if (latestVersion.isEmpty()) {
            throw new IllegalStateException("Latest version is invalid!");
        }

        if (!VersionChecker.isLatestVersion(currentVersion, latestVersion)) {
            pluginData.setLatestVersion(latestVersion);
            pluginData.setDownloadUrl(currVersionJson.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString());
            pluginData.setUpdateAvailable(true);
            return true;
        }

        return false;
    }
}
