package org.lushplugins.pluginupdater.paper.api.platform;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PaperUpdaterPlatform implements UpdaterPlatform<Player> {

    @Override
    public @Nullable PluginInfo getPlugin(String name) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null ? new PaperPluginInfo(plugin) : null;
    }

    @Override
    public List<PaperPluginInfo> getPlugins() {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
            .map(PaperPluginInfo::new)
            .toList();
    }

    @Override
    public Collection<Player> getOnlineUsers() {
        return new ArrayList<>(Bukkit.getOnlinePlayers());
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
        player.sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void broadcastActionBar(List<Player> players, String message) {
        Audience.audience(players).sendActionBar(MiniMessage.miniMessage().deserialize(message));
    }
}
