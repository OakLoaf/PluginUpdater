package org.lushplugins.pluginupdater.collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

public class SpigotCollector implements PluginCollector {

    @Override
    public List<PluginData> collectPlugins(Collection<JavaPlugin> unknownPlugins) {
        List<PluginData> pluginDataList = new ArrayList<>();
        for (JavaPlugin unknownPlugin : unknownPlugins) {
            PluginData pluginData = collectPlugin(unknownPlugin);
            if (pluginData != null) {
                pluginDataList.add(pluginData);
            }
        }

        return pluginDataList;
    }

    private @Nullable PluginData collectPlugin(JavaPlugin unknownPlugin) {
        HttpResponse<String> response;
        try {
            response = HttpUtil.sendRequest(String.format("%s/search/resources/%s", UpdaterConstants.APIs.SPIGET, unknownPlugin.getName()));
        } catch (IOException | InterruptedException e) {
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Caught error whilst getting project data from hashes: ", e);
            return null;
        }

        if (response.statusCode() != 200) {
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Received invalid response code (" + response.statusCode() + ") whilst getting project data from hashes.");
            return null;
        }

        JsonArray resultsJson = JsonParser.parseString(response.body()).getAsJsonArray();
        if (resultsJson.size() != 1) {
            return null;
        }

        JsonObject resultJson = resultsJson.get(0).getAsJsonObject();
        return new PluginData(unknownPlugin, new SpigotData(resultJson.get("id").getAsString()));
    }
}
