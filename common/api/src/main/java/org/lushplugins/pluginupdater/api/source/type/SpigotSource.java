package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.parser.RegexVersionParser;

import java.io.IOException;
import java.net.http.HttpResponse;

public class SpigotSource implements Source {
    public static final String NAME = "spigot";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        HttpResponse<String> response = HttpUtil.sendRequest("%s/resources/%s/versions/latest"
            .formatted(UpdaterConstants.Endpoint.SPIGET, resourceId));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        JsonObject pluginJson = JsonParser.parseString(response.body()).getAsJsonObject();
        String version = pluginJson.get("name").getAsString();

        return RegexVersionParser.INSTANCE.parse(version);
    }

    @Override
    public DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String resourceId))) {
            return null;
        }

        String downloadUrl = "%s/resources/%s/download".formatted(
            UpdaterConstants.Endpoint.SPIGET,
            resourceId);

        return new DownloadableRelease(downloadUrl, null, null);
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
        public String sourceName() {
            return NAME;
        }
    }
}
