package org.lushplugins.pluginupdater.velocity.api.platform;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class VelocityUpdaterPlatform implements UpdaterPlatform<Player> {
    private final ProxyServer server;
    private final Logger logger;

    public VelocityUpdaterPlatform(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public @Nullable PluginInfo getPlugin(String name) {
        PluginContainer plugin = server.getPluginManager().getPlugin(name).orElseGet(() -> {
            return server.getPluginManager().getPlugins().stream()
                .filter(container -> {
                    Optional<String> pluginName = container.getDescription().getName();
                    return pluginName.isPresent() && name.equals(pluginName.get());
                })
                .findFirst()
                .orElse(null);
        });

        return plugin != null ? new VelocityPluginInfo(plugin, logger) : null;
    }

    @Override
    public List<VelocityPluginInfo> getPlugins() {
        return server.getPluginManager().getPlugins().stream()
            .map((plugin) -> new VelocityPluginInfo(plugin, logger))
            .toList();
    }

    @Override
    public Collection<Player> getOnlineUsers() {
        return server.getAllPlayers();
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void sendMessage(Player player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void broadcastMessage(Collection<Player> players, String message) {
        Audience.audience(players).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void sendActionBar(Player player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void broadcastActionBar(List<Player> players, String message) {
        Audience.audience(players).sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }
}
