package org.lushplugins.pluginupdater.collector;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginBase;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.config.ConfigManager;

import java.util.*;
import java.util.stream.Collectors;

public interface PluginCollector {

    List<PluginData> collectPlugins(Collection<JavaPlugin> unknownPlugins);

    static List<PluginData> collectUnknownPlugins() {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        Map<String, JavaPlugin> unknownPlugins = Arrays.stream(Bukkit.getPluginManager().getPlugins())
            .map(plugin -> plugin instanceof JavaPlugin javaPlugin ? javaPlugin : null)
            .filter(plugin -> plugin != null && configManager.canRegisterPluginData(plugin.getName()))
            .collect(Collectors.toMap(PluginBase::getName, plugin -> plugin));

        if (unknownPlugins.isEmpty()) {
            return Collections.emptyList();
        }

        List<PluginData> foundPluginDataList = new ArrayList<>();
        List<PluginCollector> collectors = List.of(
            new CommonPluginCollector(),
            new PluginYamlCollector(),
            new ModrinthCollector(),
            new SpigotCollector()
        );

        for (PluginCollector collector : collectors) {
            List<PluginData> pluginDataList = collector.collectPlugins(unknownPlugins.values());

            for (PluginData pluginData : pluginDataList) {
                pluginDataList.add(pluginData);
                unknownPlugins.remove(pluginData.getPluginName());
            }
        }

        return foundPluginDataList;
    }
}
