package org.lushplugins.pluginupdater.velocity.api.util;

import java.nio.file.Path;
import java.util.Optional;

public class VelocityUtil {

    public static Optional<Path> getUpdateFolder() {
        String updateFolderName = System.getProperty("velocity.update-folder-name", "update");
        return !updateFolderName.isEmpty() ? Optional.of(Path.of("plugins", updateFolderName)) : Optional.empty();
    }
}
