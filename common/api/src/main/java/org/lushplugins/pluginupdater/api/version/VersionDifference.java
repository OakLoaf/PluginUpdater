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
     * Using same version
     */
    SAME,
    /**
     * Later than release (usually beta/dev-build)
     */
    LATER,
    UNKNOWN;

    /**
     * @return whether the VersionDifference is the same or later
     */
    public boolean isLatest() {
        return this == SAME || this == LATER;
    }

    public VersionDifference ifSameGet(Supplier<VersionDifference> supplier) {
        return this == SAME ? supplier.get() : this;
    }

    public static VersionDifference comparePreReleaseMeta(Version currentVersion, Version latestVersion) {
        String currentPreReleaseMeta = currentVersion.preReleaseMeta().orElse(null);
        String latestPreReleaseMeta = latestVersion.preReleaseMeta().orElse(null);
        if (currentPreReleaseMeta == null && latestPreReleaseMeta == null) {
            return SAME;
        }
        if (currentPreReleaseMeta == null) {
            return LATER;
        }
        if (latestPreReleaseMeta == null) {
            return BUILD;
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
                    return BUILD;
                } else if (currentPreReleaseNum > latestPreReleaseNum) {
                    return LATER;
                }
            } else if (currentMetaPartNumeric != latestMetaPartNumeric) {
                return currentMetaPartNumeric ? BUILD : LATER;
            } else {
                int asciiComparison = currentMetaPart.compareTo(latestMetaPart);
                if (asciiComparison < 0) {
                    return BUILD;
                } else if (asciiComparison > 0) {
                    return LATER;
                }
            }
        }

        if (currentPreReleaseMeta.length() < latestPreReleaseMeta.length()) {
            return BUILD;
        } else if (currentPreReleaseMeta.length() > latestPreReleaseMeta.length()) {
            return LATER;
        } else {
            return SAME;
        }
    }

    public static VersionDifference compareBuildNum(Version currentVersion, Version latestVersion) {
        return currentVersion.buildNum()
            .flatMap(currentBuild -> latestVersion.buildNum()
                .map(latestBuild -> {
                    if (currentBuild < latestBuild) {
                        return BUILD;
                    } else if (currentBuild > latestBuild) {
                        return LATER;
                    } else {
                        return SAME;
                    }
                }))
            // If either current or latest are null then we cannot compare
            .orElse(SAME);
    }
}
