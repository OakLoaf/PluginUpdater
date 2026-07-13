package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;

public record Version(
    String rawVersionString,
    String version,
    @Nullable Integer buildNum,
    @Nullable String commitHash,
    boolean potentiallyUnsafe
) {

    public Version withRawVersionString(String rawVersionString) {
        return new Version(rawVersionString, version, buildNum, commitHash, potentiallyUnsafe);
    }

    public Version withBuildNum(@Nullable Integer buildNum) {
        return new Version(rawVersionString, version, buildNum, commitHash, potentiallyUnsafe);
    }

    public Version withCommitHash(@Nullable String commitHash) {
        return new Version(rawVersionString, version, buildNum, commitHash, potentiallyUnsafe);
    }

    public Version markAsPotentiallyUnsafe(boolean potentiallyUnsafe) {
        return new Version(rawVersionString, version, buildNum, commitHash, potentiallyUnsafe);
    }

    public Version markAsPotentiallyUnsafe() {
        return markAsPotentiallyUnsafe(true);
    }
}
