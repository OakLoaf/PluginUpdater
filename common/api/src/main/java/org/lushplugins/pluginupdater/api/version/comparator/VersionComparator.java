package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

public interface VersionComparator {
    VersionDifference getVersionDifference(Version currentVersion, Version latestVersion) throws InvalidVersionFormatException;

    default VersionDifference getBuildDifference(Version currentVersion, Version latestVersion) {
        Integer currentBuild = currentVersion.buildNum();
        Integer latestBuild = latestVersion.buildNum();
        if (currentBuild == null || latestBuild == null) {
            return VersionDifference.LATEST;
        }

        return latestVersion.buildNum() > currentVersion.buildNum() ? VersionDifference.BUILD : VersionDifference.LATEST;
    }
}
