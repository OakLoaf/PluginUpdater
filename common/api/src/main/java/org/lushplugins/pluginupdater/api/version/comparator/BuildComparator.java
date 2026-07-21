package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

public class BuildComparator implements VersionComparator {
    public static final BuildComparator INSTANCE = new BuildComparator();

    @Override
    public VersionDifference compare(Version currentVersion, Version latestVersion) throws InvalidVersionFormatException {
        return VersionDifference.compareBuildDifference(currentVersion, latestVersion);
    }
}
