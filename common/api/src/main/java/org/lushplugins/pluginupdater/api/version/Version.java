package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record Version(
    String rawVersionString,
    Optional<String> version,
    Optional<String> preReleaseMeta,
    Optional<Integer> buildNum,
    Optional<String> buildMeta,
    boolean potentiallyUnsafe
) {

    public Version(
        String rawVersionString,
        @Nullable String version,
        @Nullable String preReleaseMeta,
        @Nullable Integer buildNum,
        @Nullable String buildMeta,
        boolean potentiallyUnsafe
    ) {
        this(
            rawVersionString,
            Optional.ofNullable(version),
            Optional.ofNullable(preReleaseMeta),
            Optional.ofNullable(buildNum),
            Optional.ofNullable(buildMeta),
            potentiallyUnsafe
        );
    }

    public String resolvedVersion() {
        return version.orElse(rawVersionString);
    }

    public Version withRawVersionString(String rawVersionString) {
        return new Version(rawVersionString, version, preReleaseMeta, buildNum, buildMeta, potentiallyUnsafe);
    }

    public Version withBuildNum(@Nullable Integer buildNum) {
        return new Version(rawVersionString, version, preReleaseMeta, Optional.ofNullable(buildNum), buildMeta, potentiallyUnsafe);
    }

    public Version withBuildMeta(@Nullable String commitHash) {
        return new Version(rawVersionString, version, preReleaseMeta, buildNum, Optional.ofNullable(commitHash), potentiallyUnsafe);
    }

    public Version markAsPotentiallyUnsafe(boolean potentiallyUnsafe) {
        return new Version(rawVersionString, version, preReleaseMeta, buildNum, buildMeta, potentiallyUnsafe);
    }

    public Version markAsPotentiallyUnsafe() {
        return markAsPotentiallyUnsafe(true);
    }
}
