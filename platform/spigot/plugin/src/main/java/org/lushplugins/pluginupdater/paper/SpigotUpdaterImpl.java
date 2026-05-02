package org.lushplugins.pluginupdater.paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.chatcolorhandler.paper.PaperColor;
import org.lushplugins.pluginupdater.common.platform.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;

import java.util.List;
import java.util.stream.Collectors;

public class SpigotUpdaterImpl extends UpdaterImpl {

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return BukkitLamp.builder(PluginUpdater.getInstance());
    }

    @Override
    public void sendProcessingNotification(UpdateHandler handler, UpdateHandler.ProcessingData.State state) {
        List<Player> players = Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.hasPermission("pluginupdater.notify"))
            .collect(Collectors.toUnmodifiableList());

        if (players.isEmpty()) {
            return;
        }

        int processed = handler.currentlyProcessing().getOrDefault(state, 1);
        int total = processed + handler.remainingWithState(state);

        PaperColor.handler().sendActionBarMessage(players, "&#b7faa2Updater processing: &#66b04f%s&#b7faa2/&#66b04f%s"
            .formatted(processed, total));
    }
}
