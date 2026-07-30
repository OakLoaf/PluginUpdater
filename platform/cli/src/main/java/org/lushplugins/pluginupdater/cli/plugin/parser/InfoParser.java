package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public interface InfoParser {
    ConfigParser<?> YAML_PARSER = YamlFormat.defaultInstance().createParser();

    String getResourceName();

    default Config parseResource(InputStream input) {
        return YAML_PARSER.parse(input);
    }

    default @Nullable InfoParser getFallbackInfoParser() {
        return null;
    }

    String getName(Config config);

    String getRawVersion(Config config);
}
