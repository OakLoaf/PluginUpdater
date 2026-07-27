package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;

// TODO
public class VelocityInfoParser implements InfoParser {
    public static final VelocityInfoParser INSTANCE = new VelocityInfoParser();

    @Override
    public String getResourceName() {
        return "";
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
