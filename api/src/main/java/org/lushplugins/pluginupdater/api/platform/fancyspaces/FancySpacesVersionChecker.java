package org.lushplugins.pluginupdater.api.platform.fancyspaces;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class FancySpacesVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof FancySpacesData fancySpacesData)) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/spaces/%s/versions/latest", UpdaterConstants.Endpoint.FANCYSPACES, fancySpacesData.getSpaceId()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.getAsJsonObject("data").get("version").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof FancySpacesData fancySpacesData)) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/spaces/%s/versions/latest", UpdaterConstants.Endpoint.FANCYSPACES, fancySpacesData.getSpaceId()));

        if (response.statusCode() != 200) {
            return null;
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.getAsJsonObject("data")
                .getAsJsonArray("files")
                .get(0).getAsJsonObject()
                .get("url").getAsString();
    }
}
