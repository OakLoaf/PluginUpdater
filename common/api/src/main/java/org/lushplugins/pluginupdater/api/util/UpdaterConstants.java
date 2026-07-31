package org.lushplugins.pluginupdater.api.util;

import com.google.gson.Gson;

import java.util.logging.*;

public class UpdaterConstants {
    public static final Logger LOGGER = Logger.getLogger("PluginUpdater");
    public static final Gson GSON = new Gson();

    static {
        for (Handler handler : LOGGER.getHandlers()) {
            LOGGER.removeHandler(handler);
        }

        LOGGER.addHandler(new StreamHandler(System.out, new LogFormatter()));
        LOGGER.setUseParentHandlers(false);
    }

    private static class LogFormatter extends Formatter {
        private static final String RESET = "\u001B[0m";
        private static final String YELLOW = "\u001B[33m";
        private static final String RED = "\u001B[31m";

        @Override
        public String format(LogRecord record) {
            String message = "[%s] [%s] %s".formatted(record.getLevel(), LOGGER.getName(), record.getMessage());

            if (record.getLevel() == Level.WARNING) {
                message = YELLOW + message + RESET;
            } else if (record.getLevel() == Level.SEVERE) {
                message = RED + message + RESET;
            }

            return message + System.lineSeparator();
        }
    }
}
