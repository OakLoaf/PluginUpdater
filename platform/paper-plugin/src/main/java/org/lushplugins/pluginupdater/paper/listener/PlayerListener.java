package org.lushplugins.pluginupdater.paper.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lushplugins.chatcolorhandler.paper.PaperColor;
import org.lushplugins.pluginupdater.paper.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.config.ConfigManager;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("pluginupdater.notify")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(PluginUpdater.getInstance(), () -> {
                ConfigManager configManager = PluginUpdater.getInstance().getUpdater().getConfig();

                int updatesAvailable = 0;
                for (PluginData pluginData : configManager.getAllPluginData()) {
                    if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                        updatesAvailable++;
                    }
                }

                if (updatesAvailable > 0) {
                    PaperColor.handler().sendMessage(player, configManager.getMessage("updates-available", "&#e0c01b%amount% &#ffe27aupdates are available, type &#e0c01b'%updates_command%' &#ffe27afor more information!")
                        .replace("%amount%", String.valueOf(updatesAvailable))
                        .replace("%updates_command%", "/updates list"));
                }
            }, 100);
        }
    }
}
