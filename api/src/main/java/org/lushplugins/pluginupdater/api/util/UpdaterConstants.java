package org.lushplugins.pluginupdater.api.util;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdaterConstants {
    public static final String VERSION;
    public static final Logger LOGGER = Logger.getLogger("PluginUpdater");

    static {
        Properties properties = new Properties();
        try (InputStream inputStream = UpdaterConstants.class.getClassLoader().getResourceAsStream("settings.properties")) {
            properties.load(inputStream);
            VERSION = properties.getProperty("version");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to access properties file");
            throw new IllegalStateException("Failed to access 'settings.properties' resource.");
        }
    }
}
