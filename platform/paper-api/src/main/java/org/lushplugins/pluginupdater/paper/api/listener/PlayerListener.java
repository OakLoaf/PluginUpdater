package org.lushplugins.pluginupdater.paper.api.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;

public class PlayerListener implements Listener {
    private final Updater<Player> updater;

    public PlayerListener(Updater<Player> updater) {
        this.updater = updater;

        Plugin plugin = ((PaperPluginInfo) updater.pluginInfo()).plugin();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        updater.notifier().ifPresent(notifier -> notifier.notify(event.getPlayer(), 3));
    }
}
