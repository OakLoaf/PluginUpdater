package org.lushplugins.pluginupdater.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.lushlib.listener.EventListener;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.config.ConfigManager;

public class PlayerListener implements EventListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission("pluginupdater.notify")) {
            ConfigManager configManager = PluginUpdater.getInstance().getConfigManager();

            int updatesAvailable = 0;
            for (PluginData pluginData : configManager.getAllPluginData()) {
                if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                    updatesAvailable++;
                }
            }

            if (updatesAvailable > 0) {
                ChatColorHandler.sendMessage(player, configManager.getMessage("updates-available", "&#e0c01b%amount% &#ffe27aupdates are available, type &#e0c01b'%updates_command%' &#ffe27afor more information!")
                    .replace("%amount%", String.valueOf(updatesAvailable))
                    .replace("%updates_command%", "/updates list"));
            }
        }
    }
}
