package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemVerComparator implements VersionComparator {
    public static final SemVerComparator INSTANCE = new SemVerComparator();
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+(\\.\\d+)*)");
    
    @Override
    public VersionDifference getVersionDifference(String currentVersionString, String latestVersionString) throws InvalidVersionFormatException {
        currentVersionString = applyVersionFormat(currentVersionString);
        latestVersionString = applyVersionFormat(latestVersionString);

        String[] currentVersionParts = currentVersionString.split("\\.");
        String[] latestVersionParts = latestVersionString.split("\\.");

        int i = 0;
        for (String latestVersionPart : latestVersionParts) {
            if (i >= currentVersionParts.length) {
                break;
            }

            int latestVersion = Integer.parseInt(latestVersionPart);
            int currentVersion = Integer.parseInt(currentVersionParts[i]);
            if (latestVersion > currentVersion) {
                if (i != 0) {
                    int latestVersionLast = Integer.parseInt(latestVersionParts[i - 1]);
                    int currentVersionLast = Integer.parseInt(currentVersionParts[i - 1]);
                    if (latestVersionLast >= currentVersionLast) {
                        return i == 1
                            ? VersionDifference.MINOR : i == 2
                            ? VersionDifference.PATCH : VersionDifference.BUILD;
                    }
                } else {
                    return VersionDifference.MAJOR;
                }
            } else if (latestVersion < currentVersion) {
                return VersionDifference.LATEST;
            }

            i++;
        }

        return VersionDifference.LATEST;
    }

    // TODO: Test
    private String applyVersionFormat(String versionString) throws InvalidVersionFormatException {
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.find()) {
            throw new InvalidVersionFormatException("Version ('%s') does not match required formatting '%s'"
                .formatted(versionString, VERSION_PATTERN.toString()));
        }

        return matcher.group();
    }
}
