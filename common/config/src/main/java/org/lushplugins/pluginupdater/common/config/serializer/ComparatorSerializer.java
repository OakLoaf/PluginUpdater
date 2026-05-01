package org.lushplugins.pluginupdater.common.config.serializer;

import com.electronwill.nightconfig.core.Config;
import org.lushplugins.pluginupdater.api.version.comparator.BuildNumComparator;
import org.lushplugins.pluginupdater.api.version.comparator.CalVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ComparatorSerializer {

    public static BuildNumComparator buildNum(Config config) {
        Pattern pattern = config.contains("pattern") ? Pattern.compile(config.get("pattern")) : BuildNumComparator.DEFAULT_PATTERN;
        return new BuildNumComparator(pattern);
    }

    public static CalVerComparator calVer(Config config) {
        DateTimeFormatter dateTimeFormat = config.contains("date-format") ? DateTimeFormatter.ofPattern(config.get("date-format")) : CalVerComparator.DEFAULT_FORMAT;
        return new CalVerComparator(dateTimeFormat);
    }

    public static SemVerComparator semVer(Config config) {
        Pattern pattern = config.contains("pattern") ? Pattern.compile(config.get("pattern")) : SemVerComparator.DEFAULT_PATTERN;
        return new SemVerComparator(pattern);
    }
}
