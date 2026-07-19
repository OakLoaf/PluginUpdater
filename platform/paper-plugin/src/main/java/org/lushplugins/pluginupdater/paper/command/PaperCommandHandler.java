package org.lushplugins.pluginupdater.paper.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import org.lushplugins.pluginupdater.paper.PaperUpdaterPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.command.CommandActor;

public class PaperCommandHandler implements CommandHandler {

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return BukkitLamp.builder(PaperUpdaterPlugin.getInstance())
            .defaultMessageSender((actor, rawMessage) -> {
                Component message = MiniMessage.miniMessage().deserialize(rawMessage);
                actor.sender().sendMessage(message);
            });
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return ((BukkitCommandActor) actor).sender().hasPermission(permission);
    }
}
