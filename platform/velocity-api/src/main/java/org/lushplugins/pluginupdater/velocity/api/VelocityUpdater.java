package org.lushplugins.pluginupdater.velocity.api;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.velocity.api.notification.VelocityUpdateNotifier;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;
import org.lushplugins.pluginupdater.velocity.api.util.VelocityUtil;

import java.util.List;
import java.util.logging.Logger;

public class VelocityUpdater {

    public static Updater.Builder builder(ProxyServer server, PluginContainer plugin, Logger logger) {
        return Updater.builder(new VelocityPluginInfo(plugin, logger), VelocityUtil.getUpdateFolderFile())
            .notifier((updater, permission, message) -> {
                return new VelocityUpdateNotifier(server, updater, permission, message);
            });
    }
}
