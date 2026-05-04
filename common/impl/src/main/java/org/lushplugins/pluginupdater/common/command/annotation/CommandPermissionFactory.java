package org.lushplugins.pluginupdater.common.command.annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;

public class CommandPermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<CommandActor> {
    private final UpdaterImpl updater;

    public CommandPermissionFactory(UpdaterImpl updater) {
        this.updater = updater;
    }

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<CommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        CommandPermission annotation = annotations.get(CommandPermission.class);
        if (annotation == null) {
            return null;
        }

        String permission = annotation.value();
        return actor -> updater.platform().hasPermission(actor, permission);
    }
}
