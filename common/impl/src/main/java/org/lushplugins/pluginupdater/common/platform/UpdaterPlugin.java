package org.lushplugins.pluginupdater.common.platform;

import org.lushplugins.pluginupdater.api.updater.PluginInfo;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.logging.Logger;

public interface UpdaterPlugin {

    Path getDataPath();

    Path getDownloadDir();

    InputStream getResourceStream(String path);

    InputStream getResourceStream(PluginInfo pluginInfo, String path);

    Logger getLogger();
}
