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

public record PluginYamlCollector(UpdaterImpl<?> updater) implements PluginDataCollector {

    @Override
    public List<PluginData> collect(Collection<PluginInfo> plugins) {
        List<PluginData> collectedPluginData = new ArrayList<>();
        for (PluginInfo plugin : plugins) {
            String pluginName = plugin.getName();
            if (!updater.config().canRegisterPluginData(pluginName)) {
                continue;
            }

            InputStream resource = updater.updaterPlugin().getResourceStream(plugin, "plugin.yml");
            if (resource == null) {
                resource = updater.updaterPlugin().getResourceStream(plugin, "paper-plugin.yml");

                if (resource == null) {
                    continue;
                }
            }

            Config config = YamlFormat.defaultInstance().createParser().parse(resource);

            SourceData sourceData = null;
            if (config.contains("modrinth-project-id")) {
                sourceData = ModrinthSource.Data.builder()
                    .projectId(config.get("modrinth-project-id"))
                    .releaseChannels(ModrinthSource.ReleaseChannel.ALL)
                    .build();
            }
            else if (config.contains("spigot-resource-id")) {
                sourceData = SpigotSource.Data.builder()
                    .resourceId(config.get("spigot-resource-id"))
                    .build();
            }
            else if (config.contains("hangar-project-slug")) {
                sourceData = HangarSource.Data.builder()
                    .projectSlug(config.get("hangar-project-slug"))
                    .build();
            }
            else if (config.contains("github-repo")) {
                sourceData = GithubSource.Data.builder()
                    .repo(config.get("github-repo"))
                    .build();
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
