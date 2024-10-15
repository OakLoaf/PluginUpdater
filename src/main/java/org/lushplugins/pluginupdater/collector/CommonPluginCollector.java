package org.lushplugins.pluginupdater.collector;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.platform.PlatformRegistry;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.config.ConfigManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommonPluginCollector implements PluginCollector {

    @Override
    public List<PluginData> collectPlugins(Collection<JavaPlugin> unknownPlugins) {
        ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();
        List<PluginData> pluginDataList = new ArrayList<>();

        InputStream commonPluginsInputStream = PluginUpdater.getInstance().getResource("common-plugins.yml");
        YamlConfiguration commonPluginsYml = commonPluginsInputStream != null ? YamlConfiguration.loadConfiguration(new InputStreamReader(commonPluginsInputStream)) : null;
        for (Plugin plugin : unknownPlugins) {
            String pluginName = plugin.getName();
            if (!configManager.canRegisterPluginData(pluginName)) {
                continue;
            }

            if (commonPluginsYml == null || !commonPluginsYml.contains(pluginName)) {
                continue;
            }

            ConfigurationSection pluginSection = commonPluginsYml.getConfigurationSection(pluginName);
            if (pluginSection == null) {
                continue;
            }

            PlatformData platformData = PlatformRegistry.getPlatformData(pluginSection.getString("platform"), pluginSection);
            if (platformData != null) {
                pluginDataList.add(new PluginData(plugin, platformData));
            }
        }

        return pluginDataList;
    }
}
