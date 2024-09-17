package org.lushplugins.pluginupdater.api.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lushplugins.chatcolorhandler.ChatColorHandler;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.Updater;

public class NotificationHandler implements Listener {
    private final Updater updater;
    private final String notificationPermission;
    private final String notificationMessage;

    public NotificationHandler(Updater updater, String notificationPermission, String notificationMessage) {
        this.updater = updater;
        this.notificationPermission = notificationPermission;
        this.notificationMessage = notificationMessage;

        updater.getPlugin().getServer().getPluginManager().registerEvents(this, updater.getPlugin());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (notificationPermission == null || player.hasPermission(notificationPermission)) {
            PluginData pluginData = updater.getPluginData();
            if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                Bukkit.getScheduler().runTaskLaterAsynchronously(updater.getPlugin(), () -> ChatColorHandler.sendMessage(player, notificationMessage
                    .replace("%plugin%", pluginData.getPluginName())
                    .replace("%current_version%", pluginData.getCurrentVersion())
                    .replace("%latest_version%", pluginData.getLatestVersion())
                ), 40);
            }
        }
    }
}
