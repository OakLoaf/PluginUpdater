package org.lushplugins.pluginupdater.api.version.comparator;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BuildNumComparator implements VersionComparator {
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("[.#-+]([0-9]+)$");
    public static final BuildNumComparator INSTANCE = new BuildNumComparator(DEFAULT_PATTERN);

    private final Pattern pattern;

    public BuildNumComparator(Pattern pattern) {
        this.pattern = pattern;
    }

    public BuildNumComparator(ConfigurationSection config) {
        this.pattern = config.isString("pattern") ? Pattern.compile(config.getString("pattern")) : DEFAULT_PATTERN;
    }

    @Override
    public VersionDifference getVersionDifference(String currentVersionString, String latestVersionString) throws InvalidVersionFormatException {
        int currentBuild = Integer.parseInt(applyVersionFormat(currentVersionString));
        int latestBuild = Integer.parseInt(applyVersionFormat(latestVersionString));

        return latestBuild > currentBuild ? VersionDifference.BUILD : VersionDifference.LATEST;
    }

    // TODO: Test
    private String applyVersionFormat(String versionString) throws InvalidVersionFormatException {
        Matcher matcher = this.pattern.matcher(versionString);
        if (!matcher.find()) {
            throw new InvalidVersionFormatException("Version ('%s') does not match required formatting '%s'"
                .formatted(versionString, this.pattern.toString()));
        }

        return matcher.group(1);
    }
}
