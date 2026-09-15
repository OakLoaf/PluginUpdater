package org.lushplugins.pluginupdater.api.updater;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.Nullable;

import java.io.File;

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
    ComponentLogger getComponentLogger();
}
