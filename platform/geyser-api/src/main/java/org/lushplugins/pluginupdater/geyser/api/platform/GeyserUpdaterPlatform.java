package org.lushplugins.pluginupdater.geyser.api.platform;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.session.GeyserSession;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.geyser.api.plugin.GeyserExtensionInfo;
import org.lushplugins.pluginupdater.geyser.api.util.GeyserUtil;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class GeyserUpdaterPlatform implements UpdaterPlatform<GeyserConnection> {

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
        connection.sendMessage(parseMessage(message));
    }

    @Override
    public void broadcastMessage(Collection<GeyserConnection> connections, String message) {
        String parsedMessage = parseMessage(message);
        for (GeyserConnection connection : connections) {
            connection.sendMessage(parsedMessage);
        }
    }

    @Override
    public void sendActionBar(GeyserConnection connection, String message) {
        if (connection instanceof GeyserSession session) {
            GeyserUtil.sendActionBar(session, parseMessage(message));
        }
    }

    @Override
    public void broadcastActionBar(List<GeyserConnection> connections, String message) {
        String parsedMessage = parseMessage(message);
        for (GeyserConnection connection : connections) {
            if (connection instanceof GeyserSession session) {
                GeyserUtil.sendActionBar(session, parsedMessage);
            }
        }
    }

    private static String parseMessage(String message) {
        Component component = MiniMessage.miniMessage().deserialize(message);
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
}
