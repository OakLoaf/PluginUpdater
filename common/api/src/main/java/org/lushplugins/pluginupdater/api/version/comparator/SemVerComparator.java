package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

public class SemVerComparator implements VersionComparator {
    public static final SemVerComparator INSTANCE = new SemVerComparator();
    
    @Override
    public VersionDifference getVersionDifference(Version currentVersion, Version latestVersion) throws InvalidVersionFormatException {
        String[] currentVersionParts = currentVersion.version().orElseThrow().split("\\.");
        String[] latestVersionParts = latestVersion.version().orElseThrow().split("\\.");

        int i = 0;
        for (String latestRawVersionPart : latestVersionParts) {
            if (i >= currentVersionParts.length) {
                break;
            }

            int latestVersionPart = Integer.parseInt(latestRawVersionPart);
            int currentVersionPart = Integer.parseInt(currentVersionParts[i]);
            if (latestVersionPart > currentVersionPart) {
                if (i != 0) {
                    int latestVersionLast = Integer.parseInt(latestVersionParts[i - 1]);
                    int currentVersionLast = Integer.parseInt(currentVersionParts[i - 1]);
                    if (latestVersionLast >= currentVersionLast) {
                        return i == 1 ?
                            VersionDifference.MINOR :
                            VersionDifference.PATCH;
                    }
                } else {
                    return VersionDifference.MAJOR;
                }
            } else if (latestVersionPart < currentVersionPart) {
                return VersionDifference.getBuildDifference(currentVersion, latestVersion);
            }

            i++;
        }

        return VersionDifference.getBuildDifference(currentVersion, latestVersion);
    }
}
