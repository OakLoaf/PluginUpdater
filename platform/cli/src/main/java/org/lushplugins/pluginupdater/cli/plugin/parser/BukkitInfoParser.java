package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;

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
        return config.get("name");
    }

    @Override
    public String getRawVersion(Config config) {
        return config.get("version").toString();
    }
}
