package org.lushplugins.pluginupdater.cli.plugin.parser;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.Nullable;

public interface InfoParser {

    String getResourceName();

    default @Nullable InfoParser getFallbackInfoParser() {
        return null;
    }

    String getName(Config config);

    String getVersion(Config config);
}
