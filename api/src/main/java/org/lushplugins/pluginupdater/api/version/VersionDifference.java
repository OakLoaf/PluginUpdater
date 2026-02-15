package org.lushplugins.pluginupdater.api.version;

public enum VersionDifference {
    /**
     * Contains breaking changes
     */
    MAJOR,
    MINOR,
    BUG_FIXES,
    BUILD,
    /**
     * Using same version or later than release (usually beta/dev-build)
     */
    LATEST,
    UNKNOWN
}
