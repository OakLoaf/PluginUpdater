package org.lushplugins.pluginupdater.api.platform.spigot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class SpigotVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof SpigotData spigotData)) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/resources/%s/versions/latest", UpdaterConstants.APIs.SPIGET, spigotData.getSpigotResourceId()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        JsonObject pluginJson = JsonParser.parseString(response.body()).getAsJsonObject();
        return pluginJson.get("name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) {
        return platformData instanceof SpigotData spigotData ?
            String.format("%s/resources/%s/download", UpdaterConstants.APIs.SPIGET, spigotData.getSpigotResourceId()) :
            null;
    }
}
