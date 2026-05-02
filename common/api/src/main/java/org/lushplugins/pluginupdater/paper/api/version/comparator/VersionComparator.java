package org.lushplugins.pluginupdater.paper.api.version.comparator;

import org.lushplugins.pluginupdater.paper.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.paper.api.version.VersionDifference;

public interface VersionComparator {

    VersionDifference getVersionDifference(String currentVersionString, String latestVersionString) throws InvalidVersionFormatException;
}
