package org.lushplugins.pluginupdater.cli.command;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;
import revxrsal.commands.Lamp;
import revxrsal.commands.cli.ConsoleActor;

import java.util.List;

public final class LampCompleter implements Completer {

    private final Lamp<ConsoleActor> lamp;
    private final ConsoleActor actor;

    public LampCompleter(
        Lamp<ConsoleActor> lamp,
        ConsoleActor actor
    ) {
        this.lamp = lamp;
        this.actor = actor;
    }

    @Override
    public void complete(
        LineReader reader,
        ParsedLine line,
        List<Candidate> candidates
    ) {
        String input = line.line();

        for (String suggestion : lamp.autoCompleter().complete(actor, input)) {
            candidates.add(new Candidate(suggestion));
        }
    }
}
