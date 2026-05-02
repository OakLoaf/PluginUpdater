package org.lushplugins.pluginupdater.common.platform;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.common.collector.CollectorRegistry;
import org.lushplugins.pluginupdater.common.command.UpdateCommand;
import org.lushplugins.pluginupdater.common.command.UpdaterCommand;
import org.lushplugins.pluginupdater.common.command.UpdatesCommand;
import org.lushplugins.pluginupdater.common.command.annotation.CommandPermissionFactory;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import org.lushplugins.pluginupdater.common.command.response.StringMessageResponseHandler;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.command.CommandActor;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public abstract class UpdaterImpl {
    private final CollectorRegistry collectorRegistry;
    private final UpdateHandler updateHandler;
    private final ConfigManager config;

    public UpdaterImpl() {
        collectorRegistry = new CollectorRegistry(this);
        updateHandler = new UpdateHandler(this);
        updateHandler.enable();

        config = new ConfigManager(this);
        config.reloadConfig();

        Lamp<?> lamp = prepareLamp()
            .permissionFactory(new CommandPermissionFactory(this))
            .suggestionProviders(providers -> {
                providers.addProviderForAnnotation(PluginName.class, (annotation) -> (context) -> {
                    return config.getPlugins();
                });
            })
            .responseHandler(String.class, new StringMessageResponseHandler())
            .build();
        lamp.register(new UpdaterCommand(this), new UpdatesCommand(this));

        if (config.shouldAllowDownloads()) {
            lamp.register(new UpdateCommand(this));
        }
    }

    public void shutdown() {
        updateHandler.shutdown();
    }

    public CollectorRegistry getCollectorRegistry() {
        return collectorRegistry;
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

    public abstract @Nullable PluginInfo getPlugin(String name);

    public abstract List<? extends PluginInfo> getPlugins();

    public abstract File getDownloadDir();

    public abstract Lamp.Builder<?> prepareLamp();

    public abstract boolean hasPermission(CommandActor actor, String permission);

    public abstract void sendProcessingNotification(UpdateHandler handler, UpdateHandler.ProcessingData.State state);

    public abstract Logger getLogger();
}
