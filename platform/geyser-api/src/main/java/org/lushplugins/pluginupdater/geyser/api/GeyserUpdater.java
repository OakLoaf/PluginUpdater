package org.lushplugins.pluginupdater.geyser.api;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.Extension;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.GeyserSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.geyser.api.listener.PlayerListener;
import org.lushplugins.pluginupdater.geyser.api.platform.GeyserUpdaterPlatform;
import org.lushplugins.pluginupdater.geyser.api.plugin.GeyserExtensionInfo;
import org.lushplugins.pluginupdater.geyser.api.util.GeyserUtil;

import java.util.List;

public class GeyserUpdater {
    private static boolean registriesPopulated = false;

    public static void populateRegistries() {
        if (registriesPopulated) {
            return;
        }
        registriesPopulated = true;

        SourceRegistry.register(new GeyserSource("geyser"));
        SourceRegistry.register(new ModrinthSource(List.of("geyser"), null));
        SourceRegistry.register(new SpigotSource(null));
    }

    public static Updater.Builder<GeyserConnection> builder(Extension extension, ComponentLogger logger) {
        return Updater.builder(new GeyserUpdaterPlatform(logger), new GeyserExtensionInfo(extension))
            .downloadDir(GeyserUtil.getUpdateFolder())
            .onBuild(PlayerListener::new);
    }
}
