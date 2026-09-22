package org.lushplugins.pluginupdater.common.notifier;

import org.lushplugins.pluginupdater.api.updater.PluginData;

public interface Notifier {

    void notifyDownload(PluginData pluginData);

    default void shutdown() {}
}
