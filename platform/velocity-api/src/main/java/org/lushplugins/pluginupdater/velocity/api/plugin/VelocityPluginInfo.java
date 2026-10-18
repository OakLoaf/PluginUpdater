package org.lushplugins.pluginupdater.velocity.api.plugin;

import com.velocitypowered.api.plugin.PluginContainer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;

import java.io.File;
import java.nio.file.Path;

public record VelocityPluginInfo(PluginContainer container, @Nullable ComponentLogger logger) implements PluginInfo {

    @Override
    public String getName() {
        return container.getDescription().getName().orElse(container.getDescription().getId());
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
    public ComponentLogger getComponentLogger() {
        return logger != null ? logger : UpdaterConstants.LOGGER;
    }
}
