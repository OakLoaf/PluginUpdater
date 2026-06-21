package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;

public record Version(
    String rawVersionString,
    String version,
    @Nullable Integer buildNum,
    @Nullable String commitHash
) {

    public Version withBuildNum(@Nullable Integer buildNum) {
        return new Version(rawVersionString, version, buildNum, commitHash);
    }

    public Version withCommitHash(@Nullable String commitHash) {
        return new Version(rawVersionString, version, buildNum, commitHash);
    }
}
