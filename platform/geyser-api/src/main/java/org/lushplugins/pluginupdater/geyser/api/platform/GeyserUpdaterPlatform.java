package org.lushplugins.pluginupdater.geyser.api.platform;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.Extension;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.geyser.api.plugin.GeyserExtensionInfo;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GeyserUpdaterPlatform implements UpdaterPlatform<GeyserConnection> {
    private final ComponentLogger logger;

    public GeyserUpdaterPlatform(ComponentLogger logger) {
        this.logger = logger;
    }

    @Override
    public @Nullable PluginInfo getPlugin(String name) {
        Extension extension = GeyserApi.api().extensionManager().extension(name);
        if (extension == null) {
            extension = GeyserApi.api().extensionManager().extensions().stream()
                .filter(container -> name.equals(container.description().name()))
                .findFirst()
                .orElse(null);
        }

        return extension != null ? new GeyserExtensionInfo(extension) : null;
    }

    @Override
    public List<GeyserExtensionInfo> getPlugins() {
        return GeyserApi.api().extensionManager().extensions().stream()
            .map(GeyserExtensionInfo::new)
            .toList();
    }

    @Override
    public Collection<GeyserConnection> getOnlineUsers() {
        return GeyserApi.api().onlineConnections().stream()
            .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public boolean hasPermission(GeyserConnection connection, String permission) {
        return connection.hasPermission(permission);
    }

    @Override
    public void sendMessage(GeyserConnection connection, String message) {
        connection.sendMessage(message); // TODO: Parse/strip components
    }

    @Override
    public void broadcastMessage(Collection<GeyserConnection> connections, String message) {
        String parsedMessage = message; // TODO: Parse/strip components
        for (GeyserConnection connection : connections) {
            connection.sendMessage(parsedMessage);
        }
    }

    @Override
    public void sendActionBar(GeyserConnection connection, String message) {
        connection.sendActionBar(message); // TODO: Parse/strip components
    }

    @Override
    public void broadcastActionBar(List<GeyserConnection> connections, String message) {
        String parsedMessage = message; // TODO: Parse/strip components
        for (GeyserConnection connection : connections) {
            connection.sendActionBar(parsedMessage);
        }
    }
}
