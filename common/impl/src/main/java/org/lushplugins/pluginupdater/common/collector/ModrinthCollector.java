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
import org.lushplugins.pluginupdater.common.UpdaterImpl;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.*;

public record ModrinthCollector(UpdaterImpl<?> updater) implements PluginDataCollector {

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
                updater.updaterPlugin().getComponentLogger().warn("Caught error whilst hashing plugin file: ", e);
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
            response = HttpUtil.sendRequest(String.format("%s/version_files", ModrinthSource.ENDPOINT.url()), payload);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            updater.updaterPlugin().getComponentLogger().warn("Caught error whilst getting project data from hashes: ", e);
            return Collections.emptyList();
        } catch (IOException e) {
            updater.updaterPlugin().getComponentLogger().warn("Caught error whilst getting project data from hashes: ", e);
            return Collections.emptyList();
        }

        if (response.statusCode() != 200) {
            updater.updaterPlugin().getComponentLogger().warn("Received invalid response code ({}) whilst getting project data from hashes.", response.statusCode());
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
                .sourceData(ModrinthSource.Data.builder()
                    .projectId(projectId)
                    .releaseChannels(ModrinthSource.ReleaseChannel.ALL)
                    .build())
                .build());
        });

        return pluginDataList;
    }
}
