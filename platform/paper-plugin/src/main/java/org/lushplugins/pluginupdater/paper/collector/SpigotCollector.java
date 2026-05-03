package org.lushplugins.pluginupdater.paper.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.api.source.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class SpigotCollector implements PluginDataCollector {

    @Override
    public List<PluginData> collectPluginData(Collection<PluginInfo> unknownPlugins) {
        List<PluginData> pluginDataList = new ArrayList<>();
        for (PluginInfo unknownPlugin : unknownPlugins) {
            PluginData pluginData = collectPlugin(unknownPlugin);
            if (pluginData != null) {
                pluginDataList.add(pluginData);
            }
        }

        return pluginDataList;
    }

    private @Nullable PluginData collectPlugin(PluginInfo unknownPlugin) {
        HttpResponse<String> response;
        try {
            response = HttpUtil.sendRequest(String.format("%s/search/resources/%s", UpdaterConstants.Endpoint.SPIGET, unknownPlugin.getName()));
        } catch (IOException | InterruptedException e) {
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Caught error whilst searching for project on spiget: ", e);
            return null;
        }

        if (response.statusCode() == 404) {
            return null;
        }

        if (response.statusCode() != 200) {
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Received invalid response code (%s) whilst searching for project on spiget (%s)".formatted(response.statusCode(), response.uri()));
            return null;
        }

        JsonArray resultsJson = JsonParser.parseString(response.body()).getAsJsonArray();
        if (resultsJson.size() != 1) {
            return null;
        }

        JsonObject resultJson = resultsJson.get(0).getAsJsonObject();
        return PluginData.builder(unknownPlugin)
            .sourceData(new SpigotData(resultJson.get("id").getAsString()))
            .blockDownloads()
            .build();
    }
}
