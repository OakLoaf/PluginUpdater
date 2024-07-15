package org.lushplugins.pluginupdater.api.version;

public enum VersionDifference {
    MAJOR,
    MINOR,
    BUG_FIXES,
    BUILD,
    LATEST, // Using same version or later than release (usually beta/dev-build)
    UNKNOWN;

    public static VersionDifference getVersionDifference(String localVersionRaw, String releaseVersionRaw) {
        String[] localVersionParts = localVersionRaw.split("\\.");
        String[] releaseVersionParts = releaseVersionRaw.split("\\.");

        int i = 0;
        for (String releaseVersionPart : releaseVersionParts) {
            if (i >= localVersionParts.length) {
                break;
            }

            int newVersion = Integer.parseInt(releaseVersionPart);
            int localVersion = Integer.parseInt(localVersionParts[i]);
            if (newVersion > localVersion) {
                if (i != 0) {
                    int releaseVersionLast = Integer.parseInt(releaseVersionParts[i - 1]);
                    int localVersionLast = Integer.parseInt(localVersionParts[i - 1]);
                    if (releaseVersionLast >= localVersionLast) {
                        return i == 1
                            ? VersionDifference.MINOR : i == 2
                            ? VersionDifference.BUG_FIXES : VersionDifference.BUILD;
                    }
                } else {
                    return VersionDifference.MAJOR;
                }
            } else if (newVersion < localVersion) {
                return VersionDifference.LATEST;
            }

            i++;
        }

        return VersionDifference.LATEST;
    }
}
