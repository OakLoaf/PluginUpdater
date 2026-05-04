package org.lushplugins.pluginupdater.paper.collector;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.common.config.ComparatorRegistry;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CommonPluginCollector implements PluginDataCollector {

    @Override
    public List<PluginData> collectPluginData(Collection<PluginInfo> unknownPlugins) {
        ConfigManager configManager = PluginUpdater.getInstance().updater().getConfig();
        List<PluginData> pluginDataList = new ArrayList<>();

        InputStream commonPluginsInputStream = PluginUpdater.getInstance().getResource("common-plugins.yml");
        if (commonPluginsInputStream == null) {
            return Collections.emptyList();
        }

        YamlConfiguration commonPluginsYml = YamlConfiguration.loadConfiguration(new InputStreamReader(commonPluginsInputStream));
        for (PluginInfo plugin : unknownPlugins) {
            String pluginName = plugin.getName();
            if (!configManager.canRegisterPluginData(pluginName)) {
                continue;
            }

            if (!commonPluginsYml.contains(pluginName)) {
                continue;
            }

            ConfigurationSection pluginSection = commonPluginsYml.getConfigurationSection(pluginName);
            if (pluginSection == null) {
                continue;
            }

            VersionComparator comparator;
            ConfigurationSection comparatorSection = pluginSection.getConfigurationSection("comparator");
            if (comparatorSection != null) {
                String comparatorType = comparatorSection.getString("type", "sem-ver");
                comparator = ComparatorRegistry.readVersionComparator(comparatorType, comparatorSection);
            } else {
                comparator = null;
            }

            SourceData sourceData = SourceRegistry.getSourceData(pluginSection.getString("source"), pluginSection);
            if (sourceData != null) {
                pluginDataList.add(PluginData.builder(plugin)
                    .sourceData(sourceData)
                    .comparator(comparator)
                    .build());
            }
        }

        return pluginDataList;
    }
}
