package org.lushplugins.pluginupdater.velocity.api.plugin;

import com.velocitypowered.api.plugin.PluginContainer;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public record VelocityPluginInfo(PluginContainer container, @Nullable Logger logger) implements PluginInfo {

    static {
        SourceRegistry.register(new ModrinthSource(List.of("velocity")));
    }

    @Override
    public String getName() {
        return container.getDescription().getName().orElseThrow();
    }

    @Override
    public String getVersion() {
        return container.getDescription().getVersion().orElseThrow();
    }

    @Override
    public @Nullable File getFile() {
        Path path = container.getDescription().getSource().orElse(null);
        return path != null ? path.toFile() : null;
    }

    @Override
    public Logger getLogger() {
        return logger != null ? logger : UpdaterConstants.LOGGER;
    }
}
