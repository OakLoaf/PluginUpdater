package org.lushplugins.pluginupdater.velocity.api.plugin;

import com.velocitypowered.api.plugin.PluginContainer;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

public record VelocityPluginInfo(PluginContainer plugin, @Nullable Logger logger) implements PluginInfo {

    @Override
    public String getName() {
        return plugin.getDescription().getName().orElseThrow();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion().orElseThrow();
    }

    @Override
    public @Nullable File getFile() {
        Path path = plugin.getDescription().getSource().orElse(null);
        return path != null ? path.toFile() : null;
    }

    @Override
    public Logger getLogger() {
        return logger != null ? logger : UpdaterConstants.LOGGER;
    }
}
