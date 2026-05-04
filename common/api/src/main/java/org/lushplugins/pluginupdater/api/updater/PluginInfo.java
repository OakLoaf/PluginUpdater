package org.lushplugins.pluginupdater.api.updater;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

public interface PluginInfo {

    /**
     * @return The plugin's name
     */
    String getName();

    /**
     * @return The current version of the plugin
     */
    String getVersion();

    /**
     * @return The jar file that represents this plugin
     */
    @Nullable File getFile();

    /**
     * @return The plugin's logger instance
     */
    Logger getLogger();
}
