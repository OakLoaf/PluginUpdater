package org.lushplugins.pluginupdater.paper.api.notification;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.api.listener.UpdateNotifier;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;

public class PaperUpdateNotifier extends UpdateNotifier<Player> implements Listener {

    public PaperUpdateNotifier(Updater updater, String message, String permission) {
        super(updater, message, permission);

        Plugin plugin = ((PaperPluginInfo) updater.getPluginInfo()).plugin();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean hasPermission(Player user, String permission) {
        return user.hasPermission(permission);
    }

    @Override
    public void sendMessage(Player user, String message) {
        // TODO: Add colour support
        user.sendMessage(message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        handle(event.getPlayer());
    }
}
