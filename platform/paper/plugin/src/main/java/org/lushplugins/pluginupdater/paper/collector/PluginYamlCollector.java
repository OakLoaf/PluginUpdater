package org.lushplugins.pluginupdater.paper.collector;

import org.bukkit.configuration.file.YamlConfiguration;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.platform.github.GithubData;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarData;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.config.ConfigManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PluginYamlCollector implements PluginDataCollector {

    @Override
    public List<PluginData> collectPluginData(Collection<PluginInfo> unknownPlugins) {
        ConfigManager configManager = PluginUpdater.getInstance().getUpdaterImpl().getConfig();
        List<PluginData> pluginDataList = new ArrayList<>();

        for (PluginInfo plugin : unknownPlugins) {
            String pluginName = plugin.getName();
            if (!configManager.canRegisterPluginData(pluginName)) {
                continue;
            }

            InputStream pluginInputStream = plugin.getResource("plugin.yml");
            if (pluginInputStream != null) {
                YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(new InputStreamReader(pluginInputStream));

                PlatformData platformData = null;
                if (pluginYml.contains("modrinth-project-id")) {
                    platformData = new ModrinthData(
                        pluginYml.getString("modrinth-project-id")
                    );
                }
                else if (pluginYml.contains("spigot-resource-id")) {
                    platformData = new SpigotData(
                        pluginYml.getString("spigot-resource-id")
                    );
                }
                else if (pluginYml.contains("hangar-project-slug")) {
                    platformData = new HangarData(
                        pluginYml.getString("hangar-project-slug")
                    );
                }
                else if (pluginYml.contains("github-repo")) {
                    platformData = new GithubData(
                        pluginYml.getString("github-repo"),
                        null
                    );
                }

                if (platformData != null) {
                    pluginDataList.add(PluginData.builder(plugin)
                        .platformData(platformData)
                        .build());
                }
            }
        }

        return pluginDataList;
    }
}
