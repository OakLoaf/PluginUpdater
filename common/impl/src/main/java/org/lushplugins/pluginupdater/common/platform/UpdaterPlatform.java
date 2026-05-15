package org.lushplugins.pluginupdater.common.platform;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.command.UpdateCommand;
import org.lushplugins.pluginupdater.common.command.UpdaterCommand;
import org.lushplugins.pluginupdater.common.command.UpdatesCommand;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public interface UpdaterPlatform {

    @Nullable PluginInfo getPlugin(String name);

    List<? extends PluginInfo> getPlugins();

    Path getDataPath();

    File getDownloadDir();

    InputStream getResourceStream(String path);

    InputStream getResourceStream(PluginInfo pluginInfo, String path);

    default InputStream getResourceStreamFor(String pluginName, String path) {
        return getResourceStream(getPlugin(pluginName), path);
    }

    Logger getLogger();

    Lamp.Builder<?> prepareLamp();

    default void registerLampCommands(UpdaterImpl updater, Lamp<?> lamp) {
        lamp.register(new UpdaterCommand(updater), new UpdatesCommand(updater));

        if (updater.config().shouldAllowDownloads()) {
            lamp.register(new UpdateCommand(updater));
        }
    }

    boolean hasPermission(CommandActor actor, String permission);

    void sendProcessingNotification(UpdateHandler handler, UpdateHandler.ProcessingData.State state);
}
