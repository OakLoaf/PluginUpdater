package org.lushplugins.pluginupdater.common.command.annotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.common.platform.UpdaterImpl;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.list.AnnotationList;
import revxrsal.commands.command.CommandActor;

public class CommandPermissionFactory implements revxrsal.commands.command.CommandPermission.Factory<CommandActor> {
    private final UpdaterImpl instance;

    public CommandPermissionFactory(UpdaterImpl instance) {
        this.instance = instance;
    }

    @Override
    public @Nullable revxrsal.commands.command.CommandPermission<CommandActor> create(@NotNull AnnotationList annotations, @NotNull Lamp<CommandActor> lamp) {
        CommandPermission annotation = annotations.get(CommandPermission.class);
        if (annotation == null) {
            return null;
        }

        String permission = annotation.value();
        return actor -> instance.hasPermission(actor, permission);
    }
}
