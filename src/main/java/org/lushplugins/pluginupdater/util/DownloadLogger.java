package org.lushplugins.pluginupdater.util;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.updater.PluginData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class DownloadLogger {
    private static final File LOG_FILE = new File(PluginUpdater.getInstance().getDataFolder(), "downloads.log");
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

    public static void logDownload(PluginData pluginData) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE, true));
            writer.print("[" + LocalDateTime.now().format(DATE_TIME_FORMATTER) + "] Downloaded " + pluginData.getPluginName() + ": " + pluginData.getCurrentVersion() + " -> " + pluginData.getLatestVersion() + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
