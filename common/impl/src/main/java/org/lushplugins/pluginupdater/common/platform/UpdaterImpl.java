package org.lushplugins.pluginupdater.common.platform;

import org.lushplugins.pluginupdater.common.command.UpdateCommand;
import org.lushplugins.pluginupdater.common.command.UpdaterCommand;
import org.lushplugins.pluginupdater.common.command.UpdatesCommand;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import org.lushplugins.pluginupdater.common.command.response.StringMessageResponseHandler;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.Lamp;

public abstract class UpdaterImpl {
    private final UpdateHandler updateHandler;
    private final ConfigManager config;

    public UpdaterImpl() {
        updateHandler = new UpdateHandler();
        updateHandler.enable();

        config = new ConfigManager();
        config.reloadConfig();

        Lamp<?> lamp = prepareLamp()
            .suggestionProviders(providers -> {
                providers.addProviderForAnnotation(PluginName.class, (annotation) -> (context) -> {
                    return config.getPlugins();
                });
            })
            .responseHandler(String.class, new StringMessageResponseHandler())
            .build();
        lamp.register(new UpdaterCommand(), new UpdatesCommand());

        if (config.shouldAllowDownloads()) {
            lamp.register(new UpdateCommand());
        }
    }

    public void shutdown() {
        updateHandler.shutdown();
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public ConfigManager getConfig() {
        return config;
    }

    public void reloadConfig() {
        config.reloadConfig();
    }

    public abstract Lamp.Builder<?> prepareLamp();

    public abstract void sendProcessingNotification(UpdateHandler handler, UpdateHandler.ProcessingData.State state);
}
