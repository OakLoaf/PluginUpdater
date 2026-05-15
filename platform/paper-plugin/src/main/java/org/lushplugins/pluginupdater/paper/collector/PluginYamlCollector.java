package org.lushplugins.pluginupdater.paper.collector;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.lushplugins.pluginupdater.api.source.type.GithubSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PluginYamlCollector implements PluginDataCollector {
    private final UpdaterImpl updater;

    public PluginYamlCollector(UpdaterImpl updater) {
        this.updater = updater;
    }

    @Override
    public List<PluginData> collect(Collection<PluginInfo> plugins) {
        List<PluginData> collectedPluginData = new ArrayList<>();
        for (PluginInfo plugin : plugins) {
            String pluginName = plugin.getName();
            if (!updater.config().canRegisterPluginData(pluginName)) {
                continue;
            }

            InputStream resource = updater.platform().getResourceStream("plugin.yml");
            Config config = YamlFormat.defaultInstance().createParser().parse(resource);

            SourceData sourceData = null;
            if (config.contains("modrinth-project-id")) {
                sourceData = new ModrinthSource.Data(
                    config.get("modrinth-project-id"),
                    ModrinthSource.ReleaseChannel.ALL
                );
            }
            else if (config.contains("spigot-resource-id")) {
                sourceData = new SpigotSource.Data(
                    config.get("spigot-resource-id")
                );
            }
            else if (config.contains("hangar-project-slug")) {
                sourceData = new HangarSource.Data(
                    config.get("hangar-project-slug")
                );
            }
            else if (config.contains("github-repo")) {
                sourceData = new GithubSource.Data(
                    config.get("github-repo"),
                    null
                );
            }

            if (sourceData != null) {
                collectedPluginData.add(PluginData.builder(plugin)
                    .sourceData(sourceData)
                    .build());
            }
        }

        return collectedPluginData;
    }
}
