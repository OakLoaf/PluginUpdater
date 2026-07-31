package org.lushplugins.pluginupdater.cli.plugin;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.util.logging.Logger;

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
    public Logger getLogger() {
        return UpdaterConstants.LOGGER;
    }
}
