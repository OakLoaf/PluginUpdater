package org.lushplugins.pluginupdater.cli.plugin.parser;

public class PaperInfoParser extends BukkitInfoParser {
    public static PaperInfoParser INSTANCE = new PaperInfoParser();

    @Override
    public String getResourceName() {
        return "paper-plugin.yml";
    }
}
