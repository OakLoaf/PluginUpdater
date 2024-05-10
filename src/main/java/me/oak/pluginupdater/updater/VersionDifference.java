package me.oak.pluginupdater.updater;

public enum VersionDifference {
    MAJOR,
    MINOR,
    BUG_FIXES,
    BUILD,
    LATEST // Using same version or later than release (usually beta/dev-build)
}
