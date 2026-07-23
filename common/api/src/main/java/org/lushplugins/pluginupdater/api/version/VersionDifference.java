package org.lushplugins.pluginupdater.api.version;

import org.lushplugins.pluginupdater.api.util.StringUtil;

import java.util.function.Supplier;

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

    public VersionDifference ifLatestGet(Supplier<VersionDifference> supplier) {
        return this == VersionDifference.LATEST ? supplier.get() : this;
    }

    public static VersionDifference comparePreReleaseMeta(Version currentVersion, Version latestVersion) {
        String currentPreReleaseMeta = currentVersion.buildMeta().orElse(null);
        String latestPreReleaseMeta = latestVersion.buildMeta().orElse(null);
        if (currentPreReleaseMeta == null || latestPreReleaseMeta == null) {
            return VersionDifference.LATEST;
        }

        String[] currentPreReleaseMetaParts = currentPreReleaseMeta.split("\\.");
        String[] latestPreReleaseMetaParts = latestPreReleaseMeta.split("\\.");

        for (int i = 0; i < Math.min(currentPreReleaseMetaParts.length, latestPreReleaseMetaParts.length); i++) {
            String currentMetaPart = currentPreReleaseMetaParts[i];
            String latestMetaPart = latestPreReleaseMetaParts[i];

            boolean currentMetaPartNumeric = StringUtil.isNumeric(currentMetaPart);
            boolean latestMetaPartNumeric = StringUtil.isNumeric(latestMetaPart);

            if (currentMetaPartNumeric && latestMetaPartNumeric) {
                int currentPreReleaseNum = Integer.parseInt(currentPreReleaseMeta);
                int latestPreReleaseNum = Integer.parseInt(latestPreReleaseMeta);

                if (currentPreReleaseNum < latestPreReleaseNum) {
                    return VersionDifference.BUILD;
                } else if (currentPreReleaseNum > latestPreReleaseNum) {
                    return VersionDifference.LATEST;
                }
            } else if (currentMetaPartNumeric != latestMetaPartNumeric) {
                return currentMetaPartNumeric ? VersionDifference.BUILD : VersionDifference.LATEST;
            } else {
                int asciiComparison = currentMetaPart.compareTo(latestMetaPart);

                if (asciiComparison < 0) {
                    return VersionDifference.BUILD;
                } else if (asciiComparison > 0) {
                    return VersionDifference.LATEST;
                }
            }
        }

        return currentPreReleaseMeta.length() < latestPreReleaseMeta.length() ? VersionDifference.BUILD : VersionDifference.LATEST;
    }

    public static VersionDifference compareBuildNum(Version currentVersion, Version latestVersion) {
        return currentVersion.buildNum()
            .flatMap(currentBuild -> latestVersion.buildNum()
                .map(latestBuild -> latestBuild > currentBuild  ? VersionDifference.BUILD : VersionDifference.LATEST))
            .orElse(VersionDifference.LATEST);
    }
}
