package org.lushplugins.pluginupdater.api.util;

import com.google.gson.Gson;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

public class UpdaterConstants {
    public static ComponentLogger LOGGER = ComponentLogger.logger("PluginUpdater");
    public static final Gson GSON = new Gson();
}
