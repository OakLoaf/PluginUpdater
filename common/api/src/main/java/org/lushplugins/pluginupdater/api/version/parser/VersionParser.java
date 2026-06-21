package org.lushplugins.pluginupdater.api.version.parser;

import org.lushplugins.pluginupdater.api.version.Version;

public interface VersionParser {
    Version parse(String rawVersion);
}
