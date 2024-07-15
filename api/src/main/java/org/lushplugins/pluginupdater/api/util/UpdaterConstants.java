package org.lushplugins.pluginupdater.api.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Logger;

public class UpdaterConstants {
    public static final String VERSION;
    public static final Logger LOGGER = Logger.getLogger("PluginUpdater");

    static {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        URL resource = classloader.getResource("properties.yml");

        if (resource != null) {
            YamlConfiguration propertiesConfig;
            try {
                propertiesConfig = YamlConfiguration.loadConfiguration(new File(resource.toURI()));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }

            VERSION = propertiesConfig.getString("version");
        } else {
            throw new IllegalStateException("Failed to access 'properties.yml' resource.");
        }
    }
}
