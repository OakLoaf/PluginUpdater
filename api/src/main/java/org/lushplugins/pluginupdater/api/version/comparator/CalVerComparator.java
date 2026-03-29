package org.lushplugins.pluginupdater.api.version.comparator;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class CalVerComparator implements VersionComparator {
    private static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static final CalVerComparator INSTANCE = new CalVerComparator(DEFAULT_FORMAT);

    private final DateTimeFormatter dateTimeFormat;

    public CalVerComparator(DateTimeFormatter dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public CalVerComparator(ConfigurationSection config) {
        this.dateTimeFormat = config.isString("date-format") ? DateTimeFormatter.ofPattern(config.getString("date-format")) : DEFAULT_FORMAT;
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
            TemporalAccessor accessor = this.dateTimeFormat.parse(versionString);

            // Fill missing fields with defaults
            int year = accessor.isSupported(ChronoField.YEAR) ? accessor.get(ChronoField.YEAR) : 1970;
            int month = accessor.isSupported(ChronoField.MONTH_OF_YEAR) ? accessor.get(ChronoField.MONTH_OF_YEAR) : 1;
            int day = accessor.isSupported(ChronoField.DAY_OF_MONTH) ? accessor.get(ChronoField.DAY_OF_MONTH) : 1;
            int hour = accessor.isSupported(ChronoField.HOUR_OF_DAY) ? accessor.get(ChronoField.HOUR_OF_DAY) : 0;
            int minute = accessor.isSupported(ChronoField.MINUTE_OF_HOUR) ? accessor.get(ChronoField.MINUTE_OF_HOUR) : 0;

            return LocalDateTime.of(year, month, day, hour, minute);
        } catch (DateTimeParseException e) {
            throw new InvalidVersionFormatException("Version ('%s') does not match required formatting"
                .formatted(versionString), e);
        }
    }
}
