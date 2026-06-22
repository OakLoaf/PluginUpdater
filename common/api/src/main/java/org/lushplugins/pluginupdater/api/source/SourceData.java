package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;
import org.lushplugins.pluginupdater.api.version.parser.RegexVersionParser;
import org.lushplugins.pluginupdater.api.version.parser.VersionParser;

public interface SourceData {

    String sourceName();

    default VersionParser versionParser() {
        return RegexVersionParser.INSTANCE;
    }

    default VersionComparator getDefaultComparator() {
        return SemVerComparator.INSTANCE;
    }
}
