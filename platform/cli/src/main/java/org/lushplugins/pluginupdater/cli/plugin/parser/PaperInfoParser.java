package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;

// TODO
public class PaperInfoParser implements InfoParser {
    public static PaperInfoParser INSTANCE = new PaperInfoParser();

    @Override
    public String getResourceName() {
        return "paper-plugin.yml";
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
