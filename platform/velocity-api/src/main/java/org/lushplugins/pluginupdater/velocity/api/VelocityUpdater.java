package org.lushplugins.pluginupdater.velocity.api;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.GeyserSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.velocity.api.listener.PlayerListener;
import org.lushplugins.pluginupdater.velocity.api.platform.VelocityUpdaterPlatform;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;
import org.lushplugins.pluginupdater.velocity.api.util.VelocityUtil;

import java.util.List;
import java.util.logging.Logger;

public class VelocityUpdater {
    private static boolean registriesPopulated = false;

    public static void populateRegistries(ProxyServer server) {
        if (registriesPopulated) {
            return;
        }
        registriesPopulated = true;

        SourceRegistry.register(new GeyserSource("velocity"));
        SourceRegistry.register(new HangarSource("velocity", server.getVersion().getVersion()));
        SourceRegistry.register(new ModrinthSource(List.of("velocity"), null));
    }

    public static Updater.Builder<Player> builder(ProxyServer server, PluginContainer plugin, Logger logger) {
        populateRegistries(server);

        return Updater.builder(new VelocityUpdaterPlatform(server, logger), new VelocityPluginInfo(plugin, logger))
            .downloadDir(VelocityUtil.getUpdateFolder().orElse(null))
            .onBuild((updater) -> new PlayerListener(updater, server));
    }
}
