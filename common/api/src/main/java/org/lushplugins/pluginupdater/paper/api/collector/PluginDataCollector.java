package org.lushplugins.pluginupdater.paper.api.collector;

import org.lushplugins.pluginupdater.paper.api.updater.PluginData;

import java.util.List;
import java.util.Set;

public interface PluginDataCollector {
    List<PluginData> collectPluginData(Set<String> unknownPlugins);
}
