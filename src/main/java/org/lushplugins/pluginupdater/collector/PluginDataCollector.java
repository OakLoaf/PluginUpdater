package org.lushplugins.pluginupdater.collector;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.config.ConfigManager;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public interface PluginDataCollector {

    List<PluginData> collectPlugins(Collection<JavaPlugin> unknownPlugins);

    static CompletableFuture<List<PluginData>> collectUnknownPlugins() {
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
