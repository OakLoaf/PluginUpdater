package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

public interface VersionComparator {
    VersionDifference getVersionDifference(Version currentVersion, Version latestVersion) throws InvalidVersionFormatException;

    @Deprecated(forRemoval = true)
    default VersionDifference getBuildDifference(Version currentVersion, Version latestVersion) {
        return VersionDifference.getBuildDifference(currentVersion, latestVersion);
    }
}
