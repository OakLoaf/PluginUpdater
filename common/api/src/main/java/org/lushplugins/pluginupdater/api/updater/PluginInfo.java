package org.lushplugins.pluginupdater.api.updater;

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
    File getFile();

    /**
     * @return The plugin's logger instance
     */
    Logger getLogger();
}
