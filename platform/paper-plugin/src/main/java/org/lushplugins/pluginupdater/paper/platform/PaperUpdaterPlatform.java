package org.lushplugins.pluginupdater.paper.platform;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.chatcolorhandler.paper.PaperColor;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.paper.PaperUpdaterPlugin;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PaperUpdaterPlatform implements UpdaterPlatform {
    private final Plugin plugin;

    public PaperUpdaterPlatform(Plugin plugin) {
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
    public Path getDataPath() {
        return plugin.getDataPath();
    }

    @Override
    public File getDownloadDir() {
        return Bukkit.getUpdateFolderFile();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return plugin.getResource(path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        return ((PaperPluginInfo) pluginInfo).plugin().getResource(path);
    }

    @Override
    public Logger getLogger() {
        return plugin.getLogger();
    }

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return BukkitLamp.builder(PaperUpdaterPlugin.getInstance())
            .defaultMessageSender((actor, message) -> {
                PaperColor.handler().sendMessage(actor.sender(), message);
            });
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
}
