package org.lushplugins.pluginupdater.api.platform.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ModrinthVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException {
        if (!(platformData instanceof ModrinthData modrinthData)) {
            return null;
        }

        URL url = new URL("https://api.modrinth.com/v2/project/" + modrinthData.getModrinthProjectId() + "/version?loaders=[%22bukkit%22,%22spigot%22,%22paper%22,%22purpur%22,%22folia%22]" + (modrinthData.includeFeaturedOnly() ? "&featured=true" : ""));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + UpdaterConstants.VERSION);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst getting the latest version for '" + pluginData.getPluginName() + "'.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonArray versionsJson = JsonParser.parseReader(reader).getAsJsonArray();
        JsonObject currVersionJson = versionsJson.get(0).getAsJsonObject();
        return currVersionJson.get("version_number").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) throws IOException {
        if (!(platformData instanceof ModrinthData modrinthData)) {
            return null;
        }

        String modrinthProjectSlug = modrinthData.getModrinthProjectId();

        URL url = new URL("https://api.modrinth.com/v2/project/" + modrinthProjectSlug + "/version?loaders=[%22bukkit%22,%22spigot%22,%22paper%22,%22purpur%22,%22folia%22]" + (modrinthData.includeFeaturedOnly() ? "?featured=true" : ""));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + UpdaterConstants.VERSION);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst getting the download url for '" + pluginData.getPluginName() + "'.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonArray versionsJson = JsonParser.parseReader(reader).getAsJsonArray();
        JsonObject currVersionJson = versionsJson.get(0).getAsJsonObject();
        return currVersionJson.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
    }
}
