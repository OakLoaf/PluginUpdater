package org.lushplugins.pluginupdater.common.collector;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.common.platform.UpdaterImpl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CollectorRegistry {
    private final UpdaterImpl instance;
    private final Map<String, PluginDataCollector> collectors = new LinkedHashMap<>();

    public CollectorRegistry(UpdaterImpl instance) {
        this.instance = instance;
    }

    public void register(@NotNull String id, PluginDataCollector collector) {
        if (collectors.containsKey(id)) {
            throw new IllegalStateException("This collector is already registered");
        }

        collectors.put(id, collector);
    }

    public void unregister(String id) {
        collectors.remove(id);
    }

    public CompletableFuture<List<PluginData>> collectUnknownPlugins() {
        CompletableFuture<List<PluginData>> future = new CompletableFuture<>();

        instance.getUpdateHandler().getThreads().execute(() -> {
            ConfigManager config = instance.getConfig();
            Map<String, PluginInfo> unknownPlugins = instance.getPlugins().stream()
                .filter(plugin -> config.canRegisterPluginData(plugin.getName()))
                .collect(Collectors.toMap(PluginInfo::getName, plugin -> plugin));

            if (unknownPlugins.isEmpty()) {
                future.complete(Collections.emptyList());
                return;
            }

            List<PluginData> collectedPluginData = new ArrayList<>();
            for (PluginDataCollector collector : collectors.values()) {
                List<PluginData> foundPluginData = collector.collectPluginData(unknownPlugins.values());

                collectedPluginData.addAll(foundPluginData);
                for (PluginData pluginData : foundPluginData) {
                    unknownPlugins.remove(pluginData.getPluginName());
                }
            }

            future.complete(collectedPluginData);
        });

        return future;
    }
}
