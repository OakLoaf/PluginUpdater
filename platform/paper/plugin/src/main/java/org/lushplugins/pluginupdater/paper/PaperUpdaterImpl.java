package org.lushplugins.pluginupdater.paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.chatcolorhandler.paper.PaperColor;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.platform.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.paper.plugin.PaperPluginInfo;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PaperUpdaterImpl extends UpdaterImpl {
    private final Plugin plugin;

    public PaperUpdaterImpl(Plugin plugin) {
        super();
        this.plugin = plugin;
    }

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
    public File getDownloadDir() {
        return Bukkit.getUpdateFolderFile();
    }

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return BukkitLamp.builder(PluginUpdater.getInstance());
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return ((BukkitCommandActor) actor).sender().hasPermission(permission);
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

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }
}
