package org.lushplugins.pluginupdater.geyser.command;

import net.kyori.adventure.text.Component;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import org.lushplugins.pluginupdater.geyser.GeyserUpdaterExtension;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.VelocityVisitors;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

public class GeyserCommandHandler implements CommandHandler {
    private final GeyserUpdaterExtension instance;

    public GeyserCommandHandler(GeyserUpdaterExtension instance) {
        this.instance = instance;
    }

    @Override
    public String getUpdaterCommandName() {
        return "geyserupdater";
    }

    @Override
    public String getUpdatesCommandName() {
        return "geyserupdates";
    }

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return VelocityLamp.builder(instance, instance.server())
            .defaultMessageSender((actor, rawMessage) -> {
                Component message = MiniMessage.miniMessage().deserialize(rawMessage);
                actor.source().sendMessage(message);
            });
    }

    @Override
    public void registerLampCommands(UpdaterImpl<?> updater, Lamp<?> lamp) {
        CommandHandler.super.registerLampCommands(updater, lamp);

        ((Lamp<VelocityCommandActor>) lamp).accept(VelocityVisitors.brigadier(this.instance.server()));
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return ((VelocityCommandActor) actor).source().hasPermission(permission);
    }
}
