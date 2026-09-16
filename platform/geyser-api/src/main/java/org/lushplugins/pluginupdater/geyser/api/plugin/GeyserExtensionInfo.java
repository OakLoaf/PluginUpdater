package org.lushplugins.pluginupdater.geyser.api.plugin;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.geysermc.geyser.api.extension.Extension;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;

import java.io.File;

public record GeyserExtensionInfo(Extension extension) implements PluginInfo {

    @Override
    public String getName() {
        return extension.description().name();
    }

    @Override
    public String getVersion() {
        return extension.description().version();
    }

    @Override
    public @Nullable File getFile() {
        return null;
    }

    @Override
    public ComponentLogger getComponentLogger() {
        return ComponentLogger.logger(extension.logger().prefix());
    }
}
