package org.lushplugins.pluginupdater.paper.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.paper.PaperUpdaterPlugin;
import org.lushplugins.pluginupdater.paper.platform.PaperUpdaterPlatform;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("pluginupdater.notify")) {
            return;
        }

        UpdaterImpl updater = PaperUpdaterPlugin.getInstance().updater();
        if (updater.updateHandler().remainingWithState(UpdateHandler.ProcessingData.State.SEND_NOTIFICATION) == 0) {
             String message = updater.constructUpdateMessage();
             if (message != null) {
                 PaperUpdaterPlatform platform = (PaperUpdaterPlatform) updater.platform();
                 Bukkit.getScheduler().runTaskLaterAsynchronously(PaperUpdaterPlugin.getInstance(), () -> {
                     platform.sendMessage(player, updater.constructUpdateMessage());
                 }, 100);
             }
         }
    }
}
