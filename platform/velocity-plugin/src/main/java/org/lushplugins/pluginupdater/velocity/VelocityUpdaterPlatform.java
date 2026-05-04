package org.lushplugins.pluginupdater.velocity;

import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;
import org.lushplugins.pluginupdater.velocity.api.util.VelocityUtil;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.VelocityVisitors;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class VelocityUpdaterPlatform extends UpdaterPlatform {
    private final PluginUpdater instance;

    public VelocityUpdaterPlatform(PluginUpdater instance) {
        super();
        this.instance = instance;
    }

    @Override
    public @Nullable PluginInfo getPlugin(String name) {
        PluginContainer plugin = instance.server().getPluginManager().getPlugin(name).orElse(null);
        return plugin != null ? new VelocityPluginInfo(plugin, instance.logger()) : null;
    }

    @Override
    public List<VelocityPluginInfo> getPlugins() {
        return instance.server().getPluginManager().getPlugins().stream()
            .map((plugin) -> new VelocityPluginInfo(plugin, instance.logger()))
            .toList();
    }

    @Override
    public File getDownloadDir() {
        return VelocityUtil.getUpdateFolderFile();
    }

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return VelocityLamp.builder(instance, instance.server());
    }

    @Override
    public void registerLampCommands(Lamp<?> lamp) {
        super.registerLampCommands(lamp);

        ((Lamp<VelocityCommandActor>) lamp).accept(VelocityVisitors.brigadier(instance.server()));
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return ((VelocityCommandActor) actor).source().hasPermission(permission);
    }

    @Override
    public void sendProcessingNotification(UpdateHandler handler, UpdateHandler.ProcessingData.State state) {
        List<Player> players = instance.server().getAllPlayers().stream()
            .filter(player -> player.hasPermission("pluginupdater.notify"))
            .toList();

        if (players.isEmpty()) {
            return;
        }

        int processed = handler.currentlyProcessing().getOrDefault(state, 1);
        int total = processed + handler.remainingWithState(state);

        Audience.audience(players).sendActionBar(MiniMessage.miniMessage().deserialize(
            "&#b7faa2Updater processing: &#66b04f%s&#b7faa2/&#66b04f%s"
                .formatted(processed, total)));
    }

    @Override
    public Logger getLogger() {
        return instance.logger();
    }
}
