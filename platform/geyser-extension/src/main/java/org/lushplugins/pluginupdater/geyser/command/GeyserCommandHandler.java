package org.lushplugins.pluginupdater.geyser.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import org.lushplugins.pluginupdater.geyser.GeyserUpdaterExtension;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.geyser.GeyserLamp;
import revxrsal.commands.geyser.actor.GeyserCommandActor;

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
        return GeyserLamp.builder(instance)
            .defaultMessageSender((actor, rawMessage) -> {
                Component message = MiniMessage.miniMessage().deserialize(rawMessage);
                actor.source().sendMessage(PlainTextComponentSerializer.plainText().serialize(message));
            });
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return ((GeyserCommandActor) actor).source().hasPermission(permission);
    }
}
