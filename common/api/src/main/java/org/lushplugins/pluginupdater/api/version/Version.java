package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Version(
    String rawVersionString,
    Optional<String> version,
    Optional<String> preReleaseMeta,
    Optional<Integer> buildNum,
    Optional<String> buildMeta,
    List<String> warningTags
) {

    public Version(
        String rawVersionString,
        @Nullable String version,
        @Nullable String preReleaseMeta,
        @Nullable Integer buildNum,
        @Nullable String buildMeta
    ) {
        this(
            rawVersionString,
            Optional.ofNullable(version),
            Optional.ofNullable(preReleaseMeta),
            Optional.ofNullable(buildNum),
            Optional.ofNullable(buildMeta),
            new ArrayList<>()
        );
    }

    public String resolvedVersion() {
        return version.orElse(rawVersionString);
    }

    public boolean hasWarningTags() {
        return !warningTags.isEmpty();
    }

    public void warningTag(String warningTag) {
        warningTags.add(warningTag);
    }

    public Version withRawVersionString(String rawVersionString) {
        return new Version(rawVersionString, version, preReleaseMeta, buildNum, buildMeta, warningTags);
    }

    public Version withBuildNum(@Nullable Integer buildNum) {
        return new Version(rawVersionString, version, preReleaseMeta, Optional.ofNullable(buildNum), buildMeta, warningTags);
    }

    public Version withBuildMeta(@Nullable String commitHash) {
        return new Version(rawVersionString, version, preReleaseMeta, buildNum, Optional.ofNullable(commitHash), warningTags);
    }
}
