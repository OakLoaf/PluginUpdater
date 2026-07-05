package org.lushplugins.pluginupdater.api.version.parser;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.Version;

public interface VersionParser {
    Version parse(String rawVersion) throws InvalidVersionFormatException;
}
