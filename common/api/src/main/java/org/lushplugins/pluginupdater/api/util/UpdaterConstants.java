package org.lushplugins.pluginupdater.api.util;

import com.google.gson.Gson;

import java.util.logging.Logger;

public class UpdaterConstants {
    public static final Logger LOGGER = Logger.getLogger("PluginUpdater");
    public static final Gson GSON = new Gson();

    public static class Endpoint {
        public static final String MODRINTH = "https://api.modrinth.com/v2";
        public static final String SPIGET = "https://api.spiget.org/v2";
        public static final String HANGAR = "https://hangar.papermc.io/api/v1";
        public static final String GITHUB = "https://api.github.com";
    }
}
