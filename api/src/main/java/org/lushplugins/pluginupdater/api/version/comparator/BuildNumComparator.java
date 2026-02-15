package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildNumComparator implements VersionComparator {
    public static final BuildNumComparator INSTANCE = new BuildNumComparator();
    private static final Pattern VERSION_PATTERN = Pattern.compile("[.#-]([0-9]+)$");

    @Override
    public VersionDifference getVersionDifference(String currentVersionString, String latestVersionString) throws InvalidVersionFormatException {
        int currentBuild = Integer.parseInt(applyVersionFormat(currentVersionString));
        int latestBuild = Integer.parseInt(applyVersionFormat(latestVersionString));

        return latestBuild > currentBuild ? VersionDifference.MINOR : VersionDifference.LATEST;
    }

    // TODO: Test
    private String applyVersionFormat(String versionString) throws InvalidVersionFormatException {
        Matcher matcher = VERSION_PATTERN.matcher(versionString);
        if (!matcher.find()) {
            throw new InvalidVersionFormatException("Latest version ('%s') does not match required formatting '%s'"
                .formatted(versionString, VERSION_PATTERN.toString()));
        }

        return matcher.group(1);
    }
}
