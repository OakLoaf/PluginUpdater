package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

public interface VersionComparator {

    VersionDifference getVersionDifference(String currentVersionString, String latestVersionString) throws InvalidVersionFormatException;
}
