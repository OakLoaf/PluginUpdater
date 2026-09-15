package org.lushplugins.pluginupdater.cli.plugin;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;

public record CLIPluginInfo(String name, String version, @Nullable File file) implements PluginInfo {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public @Nullable File getFile() {
        return file;
    }

    @Override
    public ComponentLogger getComponentLogger() {
        return UpdaterConstants.LOGGER;
    }
}
