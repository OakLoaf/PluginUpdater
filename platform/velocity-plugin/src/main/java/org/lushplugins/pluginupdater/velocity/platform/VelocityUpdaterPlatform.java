package org.lushplugins.pluginupdater.velocity.platform;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.velocity.VelocityUpdaterPlugin;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;
import org.lushplugins.pluginupdater.velocity.api.util.VelocityUtil;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.VelocityVisitors;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class VelocityUpdaterPlatform implements UpdaterPlatform<Player> {
    private final VelocityUpdaterPlugin instance;

    public VelocityUpdaterPlatform(VelocityUpdaterPlugin instance) {
        this.instance = instance;
    }

    @Override
    public @Nullable PluginInfo getPlugin(String name) {
        PluginContainer plugin = instance.server().getPluginManager().getPlugin(name).orElseGet(() -> {
            return instance.server().getPluginManager().getPlugins().stream()
                .filter(container -> {
                    Optional<String> pluginName = container.getDescription().getName();
                    return pluginName.isPresent() && name.equals(pluginName.get());
                })
                .findFirst()
                .orElse(null);
        });

        return plugin != null ? new VelocityPluginInfo(plugin, instance.logger()) : null;
    }

    @Override
    public List<VelocityPluginInfo> getPlugins() {
        return instance.server().getPluginManager().getPlugins().stream()
            .map((plugin) -> new VelocityPluginInfo(plugin, instance.logger()))
            .toList();
    }

    @Override
    public Path getDataPath() {
        return instance.dataFolder();
    }

    @Override
    public File getDownloadDir() {
        return VelocityUtil.getUpdateFolder();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return instance.getClass().getResourceAsStream("/" + path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        PluginContainer container = ((VelocityPluginInfo) pluginInfo).container();
        return container.getInstance().orElseThrow().getClass().getResourceAsStream(path);
    }

    @Override
    public Logger getLogger() {
        return instance.logger();
    }

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return VelocityLamp.builder(instance, instance.server())
            .defaultMessageSender((actor, message) -> {
                actor.source().sendMessage(MiniMessage.miniMessage().deserialize(message));
            });
    }

    @Override
    public String getUpdaterCommandName() {
        return "vupdater";
    }

    @Override
    public String getUpdatesCommandName() {
        return "vupdates";
    }

    @Override
    public void registerLampCommands(UpdaterImpl updater, Lamp<?> lamp) {
        UpdaterPlatform.super.registerLampCommands(updater, lamp);

        ((Lamp<VelocityCommandActor>) lamp).accept(VelocityVisitors.brigadier(this.instance.server()));
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return ((VelocityCommandActor) actor).source().hasPermission(permission);
    }

    @Override
    public void sendMessage(CommandActor actor, String message) {
        ((VelocityCommandActor) actor).source().sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void sendMessage(Player player, String message) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void broadcastNotification(String message) {
        List<Player> players = instance.server().getAllPlayers().stream()
            .filter(player -> player.hasPermission("pluginupdater.notify"))
            .toList();

        Audience.audience(players).sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Override
    public void sendProcessingActionBar(UpdateHandler handler, UpdateHandler.ProcessingData.State state) {
        List<Player> players = instance.server().getAllPlayers().stream()
            .filter(player -> player.hasPermission("pluginupdater.notify"))
            .toList();

        if (players.isEmpty()) {
            return;
        }

        int processed = handler.currentlyProcessing().getOrDefault(state, 1);
        int total = processed + handler.remainingWithState(state);

        Audience.audience(players).sendActionBar(MiniMessage.miniMessage().deserialize(
            "<#b7faa2>Updater processing: <#66b04f>%s<#b7faa2>/<#66b04f>%s"
                .formatted(processed, total)));
    }
}
