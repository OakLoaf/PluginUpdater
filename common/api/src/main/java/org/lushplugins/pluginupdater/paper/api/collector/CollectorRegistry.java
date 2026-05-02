package org.lushplugins.pluginupdater.paper.api.collector;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.pluginupdater.paper.api.updater.PluginData;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CollectorRegistry {
    private static final Map<String, PluginDataCollector> collectors = new HashMap<>();

    static {
        register("", null);
    }

    public static void register(@NotNull String id, PluginDataCollector collector) {
        if (collectors.containsKey(id)) {
            throw new IllegalStateException("This collector is already registered");
        }

        collectors.put(id, collector);
    }

    public static void unregister(String id) {
        collectors.remove(id);
    }

    public static CompletableFuture<List<PluginData>> collectUnknownPlugins() {
        CompletableFuture<List<PluginData>> future = new CompletableFuture<>();

        PluginUpdater.getInstance().getUpdateHandler().getThreads().execute(() -> {
            ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
            Map<String, JavaPlugin> unknownPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .map(plugin -> plugin instanceof JavaPlugin javaPlugin ? javaPlugin : null)
                .filter(plugin -> plugin != null && configManager.canRegisterPluginData(plugin.getName()))
                .collect(Collectors.toMap(PluginBase::getName, plugin -> plugin));

            if (unknownPlugins.isEmpty()) {
                future.complete(Collections.emptyList());
                return;
            }

            List<PluginData> foundPluginDataList = new ArrayList<>();
            List<PluginDataCollector> collectors = List.of(
                new CommonPluginCollector(),
                new PluginYamlCollector(),
                new ModrinthCollector(),
                new SpigotCollector()
            );

            for (PluginDataCollector collector : collectors) {
                List<PluginData> pluginDataList = collector.collectPlugins(unknownPlugins.values());

                foundPluginDataList.addAll(pluginDataList);
                for (PluginData pluginData : pluginDataList) {
                    unknownPlugins.remove(pluginData.getPluginName());
                }
            }

            future.complete(foundPluginDataList);
        });

        return future;
    }
}
