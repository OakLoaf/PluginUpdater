package org.lushplugins.pluginupdater.api.util;

import com.google.gson.Gson;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdaterConstants {
    public static final String VERSION;
    public static final Logger LOGGER = Logger.getLogger("PluginUpdater");
    public static final Gson GSON = new Gson();

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

    public static class Endpoint {
        public static final String MODRINTH = "https://api.modrinth.com/v2";
        public static final String SPIGET = "https://api.spiget.org/v2";
        public static final String HANGAR = "https://hangar.papermc.io/api/v1";
        public static final String GITHUB = "https://api.github.com";
        public static final String FANCYSPACES = "https://fancyspaces.net/api/v1";
    }
}
