package org.lushplugins.pluginupdater.api.util;

import java.util.regex.Pattern;

public class StringUtil {

    public static boolean isNumeric(String string) {
        return !string.isEmpty() && string.chars().allMatch(Character::isDigit);
    }

    /**
     * Apply the following regex placeholders to a {@link String}
     * <ul>
     *     <li>{@code <version>} replaced with regex pattern {@code (?<version>\d+(?:\.\d+)*)}</li>
     *     <li>{@code <build>} replaced with regex pattern {@code (?<build>\d+)}</li>
     *     <li>{@code <buildmeta>} replaced with regex pattern {@code (?<buildmeta>.+)}</li>
     *     <li>{@code <commit>} replaced with regex pattern {@code (?<buildmeta>.+)}</li>
     * </ul>
     *
     * @param rawPattern {@link String} to apply placeholders to
     * @return The parsed {@link String}
     */
    public static String applyRegexPlaceholders(String rawPattern) {
        return rawPattern
            .replace("<version>", "(?<version>\\d+(?:\\.\\d+)*)")
            .replace("<prerelease>", "(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*)")
            .replace("<build>", "(?<build>\\d+)")
            .replace("<buildmeta>", "(?<buildmeta>.+)")
            .replace("<commit>", "(?<buildmeta>.+)");
    }

    /**
     * Compare a {@link String} against a filter
     * <ul>
     *     <li>{@code =} to check that the asset name is the same as the specified string (not case-sensitive)</li>
     *     <li>{@code !} to check that the asset name does <b>not</b> contain the specified string</li>
     *     <li>{@code ?} to check that the asset name matches the string as a regex pattern</li>
     * </ul>
     *
     * @param string {@link String} to filter
     * @param filter the filter to apply to the {@link String}
     * @return whether the {@link String} matches the filter or not
     */
    public static boolean matchesFilter(String string, String filter) {
        char filterModifier = filter.charAt(0);
        return switch (filterModifier) {
            // Asset name is equal to string
            case '=' -> string.equalsIgnoreCase(filter.substring(1));
            // Asset name does not contain string
            case '!' -> !string.contains(filter.substring(1));
            // String matches regex
            case '?' -> {
                String rawPattern = applyRegexPlaceholders(filter.substring(1));
                Pattern pattern = Pattern.compile(rawPattern);
                yield pattern.matcher(string).find();
            }
            // String contains
            default -> string.contains(filter);
        };
    }
}
