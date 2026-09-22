package org.lushplugins.pluginupdater.geyser.api.util;

import org.geysermc.geyser.api.GeyserApi;

import java.nio.file.Path;

public class GeyserUtil {
    private static final Path UPDATE_FOLDER = GeyserApi.api().configDirectory()
        .getParent()
        .resolve("extensions")
        .resolve("update");

    public static Path getUpdateFolder() {
        return UPDATE_FOLDER;
    }
}
