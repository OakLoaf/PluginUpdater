package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;

// TODO
public class BukkitInfoParser implements InfoParser {
    public static BukkitInfoParser INSTANCE = new BukkitInfoParser();

    @Override
    public String getResourceName() {
        return "plugin.yml";
    }

    @Override
    public InfoParser getFallbackInfoParser() {
        return PaperInfoParser.INSTANCE;
    }

    @Override
    public String getName(Config config) {
        return "";
    }

    @Override
    public String getVersion(Config config) {
        return "";
    }
}
