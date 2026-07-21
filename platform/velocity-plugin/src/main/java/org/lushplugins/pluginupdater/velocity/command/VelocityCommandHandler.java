package org.lushplugins.pluginupdater.velocity.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import org.lushplugins.pluginupdater.velocity.VelocityUpdaterPlugin;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.velocity.VelocityLamp;
import revxrsal.commands.velocity.VelocityVisitors;
import revxrsal.commands.velocity.actor.VelocityCommandActor;

public class VelocityCommandHandler implements CommandHandler {
    private final VelocityUpdaterPlugin instance;

    public VelocityCommandHandler(VelocityUpdaterPlugin instance) {
        this.instance = instance;
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
