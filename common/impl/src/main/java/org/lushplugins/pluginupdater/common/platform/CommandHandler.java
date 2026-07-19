package org.lushplugins.pluginupdater.common.platform;

import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.command.UpdateCommand;
import org.lushplugins.pluginupdater.common.command.UpdaterCommand;
import org.lushplugins.pluginupdater.common.command.UpdatesCommand;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.orphan.Orphans;

public interface CommandHandler {

    default String getUpdaterCommandName() {
        return "updater";
    }

    default String getUpdatesCommandName() {
        return "updates";
    }

    Lamp.Builder<?> prepareLamp();

    default void registerLampCommands(UpdaterImpl<?> updater, Lamp<?> lamp) {
        lamp.register(
            Orphans.path(getUpdaterCommandName()).handler(new UpdaterCommand(updater)),
            Orphans.path(getUpdatesCommandName()).handler(new UpdatesCommand(updater))
        );

        if (updater.config().shouldAllowDownloads()) {
            lamp.register(
                Orphans.path(getUpdaterCommandName()).handler(new UpdateCommand(updater))
            );
        }
    }

    boolean hasPermission(CommandActor actor, String permission);

    default void sendMessage(CommandActor actor, String message) {
        actor.reply(message);
    }
}
