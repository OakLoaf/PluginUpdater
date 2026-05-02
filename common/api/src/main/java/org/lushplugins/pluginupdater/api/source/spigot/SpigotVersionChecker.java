package org.lushplugins.pluginupdater.api.source.spigot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.source.SourceData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class SpigotVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof SpigotData spigotData)) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/resources/%s/versions/latest", UpdaterConstants.Endpoint.SPIGET, spigotData.getResourceId()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        JsonObject pluginJson = JsonParser.parseString(response.body()).getAsJsonObject();
        return pluginJson.get("name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, SourceData sourceData) {
        return sourceData instanceof SpigotData spigotData ?
            String.format("%s/resources/%s/download", UpdaterConstants.Endpoint.SPIGET, spigotData.getResourceId()) :
            null;
    }
}
