package org.lushplugins.pluginupdater.cli.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.cli.CLILamp;
import revxrsal.commands.cli.ConsoleActor;
import revxrsal.commands.cli.actor.ActorFactory;
import revxrsal.commands.command.CommandActor;
import java.io.IOException;
import java.nio.file.Path;

public class CLICommandHandler implements CommandHandler {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final ANSIComponentSerializer ANSI =
            ANSIComponentSerializer.ansi();

    @Override
    public Lamp.Builder<?> prepareLamp() {
        return CLILamp.builder()
            .defaultMessageSender((actor, input) -> {
                Component message = MINI_MESSAGE.deserialize(input);
                actor.sendRawMessage(ANSI.serialize(message));
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerLampCommands(UpdaterImpl<?> updater, Lamp<?> lamp) {
        CommandHandler.super.registerLampCommands(updater, lamp);
        lamp.register(new StopCommand());

        startConsole((Lamp<ConsoleActor>) lamp);
    }

    private void startConsole(Lamp<ConsoleActor> lamp) {
        ConsoleActor actor = ActorFactory.defaultFactory().createForStdIo(lamp);

        try (Terminal terminal = TerminalBuilder.builder()
            .system(true)
            .build()) {

            LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .appName("plugin-updater")
                .completer(new LampCompleter(lamp, actor))
                .variable(
                    LineReader.HISTORY_FILE,
                    Path.of(".plugin-updater-history")
                )
                .option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
                .option(LineReader.Option.HISTORY_BEEP, false)
                .build();

            printWelcome(terminal);

            while (true) {
                try {
                    String input = reader.readLine("> ");

                    if (input.isBlank()) {
                        continue;
                    }

                    lamp.dispatch(actor, input);

                } catch (UserInterruptException ignored) {
                    // Ctrl+C
                    lamp.dispatch(actor, "stop");
                    break;
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize CLI terminal", e);
        }
    }

    private void printWelcome(Terminal terminal) {
        terminal.writer().println();
        terminal.writer().println(
            ANSI.serialize(MINI_MESSAGE.deserialize(
                "<gradient:#5e81ac:#88c0d0><bold>PluginUpdater CLI</bold></gradient>"
            ))
        );
        terminal.writer().println(
            ANSI.serialize(MINI_MESSAGE.deserialize(
                "<dark_gray>Type <white>upd <tab></white> for available commands. Visit https://github.com/OakLoaf/PluginUpdater/wiki/PluginUpdater-CLI for more infos."
            ))
        );
        terminal.writer().println();
        terminal.writer().flush();
    }

    @Override
    public boolean hasPermission(CommandActor actor, String permission) {
        return true;
    }
}
