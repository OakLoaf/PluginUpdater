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
        return currentVersion.buildNum()
            .flatMap(currentBuild -> latestVersion.buildNum()
                .map(latestBuild -> latestBuild > currentBuild  ? VersionDifference.BUILD : VersionDifference.LATEST))
            .orElse(VersionDifference.LATEST);
    }
}
