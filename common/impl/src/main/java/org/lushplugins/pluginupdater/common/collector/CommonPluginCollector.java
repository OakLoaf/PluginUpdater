package org.lushplugins.pluginupdater.common.collector;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.config.deserializer.PluginDataDeserializer;

import java.io.InputStream;
import java.util.*;

public class CommonPluginCollector implements PluginDataCollector {
    private final UpdaterImpl updater;

    public CommonPluginCollector(UpdaterImpl updater) {
        this.updater = updater;
    }

    @Override
    public List<PluginData> collect(Collection<PluginInfo> plugins) {
        InputStream resource = updater.platform().getResourceStream("common-plugins.yml");
        Config config = YamlFormat.defaultInstance().createParser().parse(resource);

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

        return collectedPluginData;
    }
}
