package org.lushplugins.pluginupdater.api.version.parser;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.Version;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexVersionParser implements VersionParser {
    public static final String DEFAULT_FORMAT = "<version>";
    public static final RegexVersionParser INSTANCE = new RegexVersionParser(DEFAULT_FORMAT);

    private final Pattern pattern;

    public RegexVersionParser(String versionFormat) {
        this.pattern = parsePattern(versionFormat);
    }

    @Override
    public Version parse(String rawVersion) {
        Matcher matcher = this.pattern.matcher(rawVersion);
        if (!matcher.find()) {
            throw new InvalidVersionFormatException("Version ('%s') does not match required formatting '%s'"
                .formatted(rawVersion, this.pattern.toString()));
        }

        String version = getNamedGroupContent(matcher, "version");

        String rawBuildNum = getNamedGroupContent(matcher, "build");
        Integer buildNum = rawBuildNum != null ? Integer.parseInt(rawBuildNum) : null;

        String commitHash = getNamedGroupContent(matcher, "commit");

        return new Version(rawVersion, version, buildNum, commitHash);
    }

    public static Pattern parsePattern(String rawPattern) {
        return Pattern.compile(rawPattern
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("<version>", "(?<version>\\d+(?:\\.\\d+)*)")
            .replace("<build>", "(?<build>\\d+")
            .replace("<commit>", "(?<commit>.+)"));
    }

    public static String getNamedGroupContent(Matcher matcher, String namedGroup) {
        Map<String, Integer> namedGroups = matcher.namedGroups();
        if (namedGroups.containsKey(namedGroup)) {
            int groupIndex = namedGroups.get(namedGroup);
            return matcher.group(groupIndex);
        } else {
            return null;
        }
    }
}
