package org.lushplugins.pluginupdater.api.version;

public enum VersionDifference {
    /**
     * Contains breaking changes
     */
    MAJOR,
    MINOR,
    PATCH,
    BUILD,
    /**
     * Using same version or later than release (usually beta/dev-build)
     */
    LATEST,
    UNKNOWN;

    public static VersionDifference getBuildDifference(Version currentVersion, Version latestVersion) {
        Integer currentBuild = currentVersion.buildNum();
        Integer latestBuild = latestVersion.buildNum();
        if (currentBuild == null || latestBuild == null) {
            return VersionDifference.LATEST;
        }

        return latestVersion.buildNum() > currentVersion.buildNum() ? VersionDifference.BUILD : VersionDifference.LATEST;
    }
}
