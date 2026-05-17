package org.lushplugins.pluginupdater.api.util;

import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class DownloadLogger {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
        .append(DateTimeFormatter.ISO_LOCAL_DATE)
        .appendLiteral(' ')
        .appendValue(HOUR_OF_DAY, 2)
        .appendLiteral(':')
        .appendValue(MINUTE_OF_HOUR, 2)
        .optionalStart()
        .appendLiteral(':')
        .appendValue(SECOND_OF_MINUTE, 2)
        .toFormatter();
    private static File logFile;


    public static void setLogFile(File file) {
        logFile = file;
    }

    public static void logDownload(PluginData pluginData) {
        if (logFile == null) {
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
            writer.print("[" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + "] Downloaded " + pluginData.getPluginName() + ": " + pluginData.getCurrentVersion() + " -> " + pluginData.getLatestVersion() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
