package org.lushplugins.pluginupdater.api.platform.modrinth;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class ModrinthVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof ModrinthData modrinthData)) {
            return null;
        }

        JsonObject currVersionJson = getLatestVersion(pluginData, modrinthData);
        return currVersionJson.get("version_number").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof ModrinthData modrinthData)) {
            return null;
        }

        JsonObject currVersionJson = getLatestVersion(pluginData, modrinthData);
        return currVersionJson.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
    }

    private JsonArray getVersions(PluginData pluginData, ModrinthData modrinthData) throws IOException, InterruptedException {
        StringBuilder uriBuilder = new StringBuilder(String.format("%s/project/%s/version", UpdaterConstants.APIs.MODRINTH, modrinthData.getModrinthProjectId()))
            .append("?loaders=[%22bukkit%22,%22spigot%22,%22paper%22,%22purpur%22,%22folia%22]");

        if (modrinthData.specifiesVersionType()) {
            uriBuilder.append("&version_type=").append(modrinthData.getVersionType());
        }

        if (modrinthData.includeFeaturedOnly()) {
            uriBuilder.append("&featured=true");
        }

        HttpResponse<String> response = HttpUtil.sendRequest(uriBuilder.toString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (%s) whilst checking '%s' for updates.".formatted(response.statusCode(), pluginData.getPluginName()));
        }

        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    private JsonObject getLatestVersion(PluginData pluginData, ModrinthData modrinthData) throws IOException, InterruptedException {
        JsonArray versions = getVersions(pluginData, modrinthData);
        if (versions.isEmpty()) {
            throw new IllegalStateException("Failed to collect versions for '%s'".formatted(pluginData.getPluginName() ));
        }

        return versions.get(0).getAsJsonObject();
    }
}
