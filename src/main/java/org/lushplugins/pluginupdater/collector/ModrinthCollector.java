package org.lushplugins.pluginupdater.collector;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.logging.Level;

public class ModrinthCollector implements PluginDataCollector {

    @Override
    public List<PluginData> collectPlugins(Collection<JavaPlugin> unknownPlugins) {
        HashMap<String, JavaPlugin> pluginHashes = new HashMap<>();
        for (JavaPlugin unknownPlugin : unknownPlugins) {
            File pluginFile = getPluginFile(unknownPlugin);
            if (pluginFile == null) {
                continue;
            }

            HashCode hash;
            try {
                hash = Files.asByteSource(pluginFile).hash(Hashing.sha512());
                pluginHashes.put(hash.toString(), unknownPlugin);
            } catch (IOException e) {
                PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Caught error whilst hashing plugin file: ", e);
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
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Caught error whilst getting project data from hashes: ", e);
            return Collections.emptyList();
        }

        if (response.statusCode() != 200) {
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Received invalid response code (" + response.statusCode() + ") whilst getting project data from hashes.");
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
            pluginDataList.add(new PluginData(plugin, new ModrinthData(projectId, true)));
        });

        return pluginDataList;
    }

    private @Nullable File getPluginFile(JavaPlugin plugin) {
        try {
            Method method = JavaPlugin.class.getDeclaredMethod("getFile");
            method.setAccessible(true);

            return (File) method.invoke(plugin);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Caught error whilst getting plugin file: ", e);
            return null;
        }
    }
}
