package org.lushplugins.pluginupdater.common.platform;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;

import java.io.InputStream;
import java.nio.file.Path;

public interface UpdaterPlugin {

    Path getDataPath();

    Path getDownloadDir();

    InputStream getResourceStream(String path);

    InputStream getResourceStream(PluginInfo pluginInfo, String path);

    ComponentLogger getComponentLogger();
}
