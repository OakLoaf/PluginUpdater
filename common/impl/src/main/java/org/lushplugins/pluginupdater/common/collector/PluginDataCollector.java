package org.lushplugins.pluginupdater.common.collector;

import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.UpdaterImpl;

import java.util.Collection;
import java.util.List;

@FunctionalInterface
public interface PluginDataCollector {
    List<PluginData> collect(Collection<PluginInfo> plugins);

    static Factory of(PluginDataCollector collector) {
        return (ignored) -> collector;
    }

    @FunctionalInterface
    interface Factory {
        PluginDataCollector create(UpdaterImpl updater);
    }
}
