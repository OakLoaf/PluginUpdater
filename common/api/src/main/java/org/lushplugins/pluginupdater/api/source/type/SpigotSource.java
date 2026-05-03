package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;

import java.io.IOException;
import java.net.http.HttpResponse;

public class SpigotSource implements Source {

    @Override
    public String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest("%s/resources/%s/versions/latest"
            .formatted(UpdaterConstants.Endpoint.SPIGET, resourceId));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        JsonObject pluginJson = JsonParser.parseString(response.body()).getAsJsonObject();
        return pluginJson.get("name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, SourceData sourceData) {
        return sourceData instanceof Data(String resourceId) ?
            String.format("%s/resources/%s/download", UpdaterConstants.Endpoint.SPIGET, resourceId) :
            null;
    }

    @Override
    public int getRateLimit() {
        return 1;
    }

    /**
     * @param resourceId The Spigot Resource id
     */
    public record Data(String resourceId) implements SourceData {

        @Override
        public String name() {
            return "spigot";
        }
    }
}
