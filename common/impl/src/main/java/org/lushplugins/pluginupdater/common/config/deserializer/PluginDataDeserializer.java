package org.lushplugins.pluginupdater.common.config.deserializer;

import com.electronwill.nightconfig.core.Config;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.config.ComparatorRegistry;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PluginDataDeserializer {

    public static PluginData deserialize(UpdaterImpl updater, String pluginName, Config config) {
        PluginInfo currPlugin = updater.platform().getPlugin(pluginName);
        if (currPlugin == null) {
            return null;
        }

        VersionComparator comparator;
        Config comparatorConfig = config.get("comparator");
        if (comparatorConfig != null) {
            String comparatorType = comparatorConfig.getOrElse("type", "sem-ver");
            comparator = ComparatorRegistry.deserializeVersionComparator(comparatorType, comparatorConfig);
        } else {
            comparator = null;
        }

        List<String> tags = config.getOrElse("tags", Collections.emptyList());
        boolean allowDownloads = config.getOrElse("allow-downloads", true);

        try {
            SourceData sourceData = SourceDataDeserializer.deserialize(updater.platform(), config);
            if (sourceData != null) {
                return PluginData.builder(currPlugin)
                    .sourceData(sourceData)
                    .comparator(comparator)
                    .tags(tags)
                    .allowDownloads(allowDownloads)
                    .build();
            }
        } catch (Exception e) {
            updater.platform().getLogger().log(Level.SEVERE, "Caught error whilst reading data for '%s'"
                .formatted(pluginName), e);
        }

        return null;
    }
}
