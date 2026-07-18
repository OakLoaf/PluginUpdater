package org.lushplugins.pluginupdater.api.util;

import java.util.regex.Pattern;

public class StringComparison {

    /**
     * Compare a string against a filter
     * <ul>
     *     <li>`=` to check that the asset name is the same as the specified string (not case-sensitive)</li>
     *     <li>`!` to check that the asset name does **not** contain the specified string</li>
     *     <li>`?` to check that the asset name matches the string as a regex pattern</li>
     * </ul>

     * @param string string to filter
     * @param filter the filter to apply to the string
     * @return whether the string matches the filter or not
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
                Pattern pattern = Pattern.compile(filter.substring(1));
                yield pattern.matcher(string).find();
            }
            // String contains
            default -> string.contains(filter);
        };
    }
}
