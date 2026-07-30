package org.lushplugins.pluginupdater.cli.command;

import revxrsal.commands.annotation.Command;

@SuppressWarnings("unused")
public class StopCommand {

    @Command("stop")
    public void stop() {
        System.exit(0);
    }
}
