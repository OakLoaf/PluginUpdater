package org.lushplugins.pluginupdater.common.collector;

import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface PluginDataCollector {
    List<PluginData> collectPluginData(Collection<PluginInfo> unknownPlugins);
}
