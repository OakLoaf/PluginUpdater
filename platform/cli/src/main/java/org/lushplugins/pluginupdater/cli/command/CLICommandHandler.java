package org.lushplugins.pluginupdater.cli.command;

import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.cli.CLILamp;
import revxrsal.commands.command.CommandActor;

public class CLICommandHandler implements CommandHandler {

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return CLILamp.builder()
            .defaultMessageSender(CommandActor::sendRawMessage); // TODO: Strip adventure formatting tags
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return true;
    }
}
