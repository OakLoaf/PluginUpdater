package org.lushplugins.pluginupdater.paper.collector;

import org.bukkit.configuration.file.YamlConfiguration;
import org.lushplugins.pluginupdater.api.source.type.GithubSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.api.source.SourceData;
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
        ConfigManager configManager = PluginUpdater.getInstance().getUpdater().getConfig();
        List<PluginData> pluginDataList = new ArrayList<>();

        for (PluginInfo plugin : unknownPlugins) {
            String pluginName = plugin.getName();
            if (!configManager.canRegisterPluginData(pluginName)) {
                continue;
            }

            InputStream pluginInputStream = plugin.getResource("plugin.yml");
            if (pluginInputStream != null) {
                YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(new InputStreamReader(pluginInputStream));

                SourceData sourceData = null;
                if (pluginYml.contains("modrinth-project-id")) {
                    sourceData = new ModrinthSource.Data(
                        pluginYml.getString("modrinth-project-id"),
                        ModrinthSource.ReleaseChannel.ALL
                    );
                }
                else if (pluginYml.contains("spigot-resource-id")) {
                    sourceData = new SpigotSource.Data(
                        pluginYml.getString("spigot-resource-id")
                    );
                }
                else if (pluginYml.contains("hangar-project-slug")) {
                    sourceData = new HangarSource.Data(
                        pluginYml.getString("hangar-project-slug")
                    );
                }
                else if (pluginYml.contains("github-repo")) {
                    sourceData = new GithubSource.Data(
                        pluginYml.getString("github-repo"),
                        null
                    );
                }

                if (sourceData != null) {
                    pluginDataList.add(PluginData.builder(plugin)
                        .sourceData(sourceData)
                        .build());
                }
            }
        }

        return pluginDataList;
    }
}
