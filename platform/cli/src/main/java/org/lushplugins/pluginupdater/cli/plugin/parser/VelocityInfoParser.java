package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.json.JsonFormat;

import java.io.InputStream;

public class VelocityInfoParser implements InfoParser {
    private static final ConfigParser<?> JSON_PARSER = JsonFormat.minimalInstance().createParser();
    public static final VelocityInfoParser INSTANCE = new VelocityInfoParser();

    @Override
    public String getResourceName() {
        return "velocity-plugin.json";
    }

    @Override
    public Config parseResource(InputStream input) {
        return JSON_PARSER.parse(input);
    }

    @Override
    public String getName(Config config) {
        return config.get("name");
    }

    @Override
    public String getRawVersion(Config config) {
        return config.get("version");
    }
}
