package org.lushplugins.pluginupdater.paper.collector;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.paper.api.platform.PlatformData;
import org.lushplugins.pluginupdater.paper.api.platform.github.GithubData;
import org.lushplugins.pluginupdater.paper.api.platform.hangar.HangarData;
import org.lushplugins.pluginupdater.paper.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.paper.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.paper.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.config.ConfigManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PluginYamlCollector implements PluginDataCollector {

    @Override
    public List<PluginData> collectPlugins(Collection<JavaPlugin> unknownPlugins) {
        ConfigManager configManager = PluginUpdater.getInstance().getUpdaterImpl().getConfig();
        List<PluginData> pluginDataList = new ArrayList<>();

        for (Plugin plugin : unknownPlugins) {
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
