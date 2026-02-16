package org.lushplugins.pluginupdater.api.version.comparator;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CalVerComparator implements VersionComparator {
    private static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static final CalVerComparator INSTANCE = new CalVerComparator(DEFAULT_FORMAT);

    private final DateTimeFormatter dateTimeFormat;

    public CalVerComparator(DateTimeFormatter dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    /**
     * @return {@link VersionDifference#LATEST} if the latest version has the same (or earlier) date and time
     * <br> {@link VersionDifference#MINOR} if the latest version has a later date
     * <br> {@link VersionDifference#PATCH} if the latest version has the same date but later time
     */
    @Override
    public VersionDifference getVersionDifference(String currentVersionString, String latestVersionString) throws InvalidVersionFormatException {
        LocalDateTime currentVersion = parseDateTime(currentVersionString);
        LocalDateTime latestVersion = parseDateTime(latestVersionString);
        if (!latestVersion.isAfter(currentVersion)) {
            return VersionDifference.LATEST;
        }

        LocalDate currentVersionDate = currentVersion.toLocalDate();
        LocalDate latestVersionDate = latestVersion.toLocalDate();
        return latestVersionDate.isEqual(currentVersionDate) ? VersionDifference.PATCH : VersionDifference.MINOR;
    }

    private LocalDateTime parseDateTime(String versionString) throws InvalidVersionFormatException {
        try {
            return LocalDateTime.parse(versionString, this.dateTimeFormat);
        } catch (DateTimeParseException e) {
            throw new InvalidVersionFormatException("Version ('%s') does not match required formatting"
                .formatted(versionString), e);
        }
    }
}
