package org.lushplugins.pluginupdater.common.collector;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Level;

public class ModrinthCollector implements PluginDataCollector {
    private final UpdaterPlatform platform;

    public ModrinthCollector(UpdaterPlatform platform) {
        this.platform = platform;
    }

    @Override
    public List<PluginData> collect(Collection<PluginInfo> plugins) {
        HashMap<String, PluginInfo> pluginHashes = new HashMap<>();
        for (PluginInfo unknownPlugin : plugins) {
            File pluginFile = unknownPlugin.getFile();
            if (pluginFile == null) {
                continue;
            }

            HashCode hash;
            try {
                hash = Files.asByteSource(pluginFile).hash(Hashing.sha512());
                pluginHashes.put(hash.toString(), unknownPlugin);
            } catch (IOException e) {
                platform.getLogger().log(Level.WARNING, "Caught error whilst hashing plugin file: ", e);
            }
        }

        if (pluginHashes.isEmpty()) {
            return Collections.emptyList();
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("algorithm", "sha512");
        payload.add("hashes", UpdaterConstants.GSON.toJsonTree(pluginHashes.keySet()).getAsJsonArray());

        HttpResponse<String> response;
        try {
            response = HttpUtil.sendRequest(String.format("%s/version_files", UpdaterConstants.Endpoint.MODRINTH), payload);
        } catch (IOException | InterruptedException e) {
            platform.getLogger().log(Level.WARNING, "Caught error whilst getting project data from hashes: ", e);
            return Collections.emptyList();
        }

        if (response.statusCode() != 200) {
            platform.getLogger().log(Level.WARNING, "Received invalid response code (" + response.statusCode() + ") whilst getting project data from hashes.");
            return Collections.emptyList();
        }

        List<PluginData> pluginDataList = new ArrayList<>();
        JsonObject versionsJson = JsonParser.parseString(response.body()).getAsJsonObject();

        pluginHashes.forEach((hash, plugin) -> {
            JsonObject versionJson = versionsJson.getAsJsonObject(hash);
            if (versionJson == null) {
                return;
            }

            String projectId = versionJson.get("project_id").getAsString();
            pluginDataList.add(PluginData.builder(plugin)
                .sourceData(new ModrinthSource.Data(projectId, ModrinthSource.ReleaseChannel.ALL))
                .build());
        });

        return pluginDataList;
    }
}
