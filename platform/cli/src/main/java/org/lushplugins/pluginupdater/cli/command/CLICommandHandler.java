package org.lushplugins.pluginupdater.cli.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.cli.CLILamp;
import revxrsal.commands.cli.ConsoleActor;
import revxrsal.commands.command.CommandActor;

public class CLICommandHandler implements CommandHandler {

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return CLILamp.builder()
            .defaultMessageSender((actor, input) -> {
                actor.sendRawMessage(MiniMessage.miniMessage().stripTags(input));
            });
    }

    @Override
    public void registerLampCommands(UpdaterImpl<?> updater, Lamp<?> lamp) {
        CommandHandler.super.registerLampCommands(updater, lamp);
        ((Lamp<ConsoleActor>) lamp).accept(CLILamp.pollStdin());
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return true;
    }
}
