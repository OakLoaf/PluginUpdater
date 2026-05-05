package org.lushplugins.pluginupdater.common.collector;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.config.deserializer.PluginDataDeserializer;

import java.util.*;

public class CommonPluginCollector implements PluginDataCollector {
    private final UpdaterImpl updater;

    public CommonPluginCollector(UpdaterImpl updater) {
        this.updater = updater;
    }

    @Override
    public List<PluginData> collect(Collection<PluginInfo> plugins) {
        // TODO: Access resource location (not file)
        FileConfig config = FileConfig.of("common-plugins.yml");
        config.load();

        List<PluginData> collectedPluginData = new ArrayList<>();
        for (PluginInfo plugin : plugins) {
            String pluginName = plugin.getName();
            if (!updater.config().canRegisterPluginData(pluginName)) {
                continue;
            }

            if (!config.contains(pluginName)) {
                continue;
            }

            Config pluginConfig = config.get(pluginName);
            PluginData pluginData = PluginDataDeserializer.deserialize(updater, pluginName, pluginConfig);
            if (pluginData != null) {
                collectedPluginData.add(pluginData);
            }
        }

        config.close();
        return collectedPluginData;
    }
}
