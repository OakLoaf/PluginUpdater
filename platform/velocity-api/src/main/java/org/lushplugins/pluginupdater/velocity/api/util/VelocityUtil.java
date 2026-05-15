package org.lushplugins.pluginupdater.velocity.api.util;

import java.io.File;
import java.nio.file.Path;

public class VelocityUtil {

    public static File getUpdateFolderFile() {
        String updateFolderName = System.getProperty("velocity.update-folder-name", "update");
        return !updateFolderName.isEmpty() ? Path.of("plugins", updateFolderName).toFile() : null;
    }
}
