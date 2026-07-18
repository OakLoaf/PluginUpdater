package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record Version(
    String rawVersionString,
    Optional<String> version,
    Optional<Integer> buildNum,
    Optional<String> commitHash,
    boolean potentiallyUnsafe
) {

    public Version(
        String rawVersionString,
        @Nullable String version,
        @Nullable Integer buildNum,
        @Nullable String commitHash,
        boolean potentiallyUnsafe
    ) {
        this(
            rawVersionString,
            Optional.ofNullable(version),
            Optional.ofNullable(buildNum),
            Optional.ofNullable(commitHash),
            potentiallyUnsafe
        );
    }

    public String resolvedVersion() {
        return version.orElse(rawVersionString);
    }

    public Version withRawVersionString(String rawVersionString) {
        return new Version(rawVersionString, version, buildNum, commitHash, potentiallyUnsafe);
    }

    public Version withBuildNum(@Nullable Integer buildNum) {
        return new Version(rawVersionString, version, Optional.ofNullable(buildNum), commitHash, potentiallyUnsafe);
    }

    public Version withCommitHash(@Nullable String commitHash) {
        return new Version(rawVersionString, version, buildNum, Optional.ofNullable(commitHash), potentiallyUnsafe);
    }

    public Version markAsPotentiallyUnsafe(boolean potentiallyUnsafe) {
        return new Version(rawVersionString, version, buildNum, commitHash, potentiallyUnsafe);
    }

    public Version markAsPotentiallyUnsafe() {
        return markAsPotentiallyUnsafe(true);
    }
}
