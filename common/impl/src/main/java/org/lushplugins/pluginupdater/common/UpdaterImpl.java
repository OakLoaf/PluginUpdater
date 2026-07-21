package org.lushplugins.pluginupdater.common;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.common.command.annotation.CommandPermissionFactory;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import org.lushplugins.pluginupdater.common.command.response.StringMessageResponseHandler;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.common.platform.CommandHandler;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.Lamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UpdaterImpl<T> {
    private final UpdaterPlatform<T> platform;
    private final UpdaterPlugin updaterPlugin;
    private final CommandHandler commandPlatform;
    private final List<PluginDataCollector.Factory> collectors;
    private final UpdateHandler<T> updateHandler;
    private final ConfigManager config;

    public UpdaterImpl(UpdaterPlatform<T> platform, UpdaterPlugin updaterPlugin, CommandHandler commandPlatform, List<PluginDataCollector.Factory> collectors) {
        this.platform = platform;
        this.updaterPlugin = updaterPlugin;
        this.commandPlatform = commandPlatform;
        this.collectors = collectors;

        updateHandler = new UpdateHandler<>(this);
        updateHandler.enable();

        config = new ConfigManager(this);
        config.reload();

        Lamp<?> lamp = commandPlatform.prepareLamp()
            .permissionFactory(new CommandPermissionFactory(this))
            .parameterTypes(parameterTypes -> parameterTypes
                .addContextParameter(UpdaterImpl.class, (parameter, context) -> this))
            .suggestionProviders(providers -> providers
                .addProviderForAnnotation(PluginName.class, (annotation) -> (context) -> {
                    if (annotation.includeTags()) {
                        switch (context.input().peek()) {
                            case '$' -> {
                                return SourceRegistry.values().stream()
                                    .map(source -> "$" + source.getName())
                                    .toList();
                            }
                            case '#' -> {
                                return config.getAllPluginData().stream()
                                    .flatMap(plugin -> plugin.getTags().stream())
                                    .distinct()
                                    .map(tag -> "#" + tag)
                                    .toList();
                            }
                        }
                    }

                    if (annotation.withUpdateAvailable()) {
                        return config.getAllPluginData().stream()
                            .filter(PluginData::isUpdateAvailable)
                            .map(PluginData::pluginName)
                            .toList();
                    }

                    return config.getPlugins();
                })
            )
            .responseHandler(String.class, new StringMessageResponseHandler())
            .build();

        commandPlatform.registerLampCommands(this, lamp);
    }

    public void shutdown() {
        updateHandler.shutdown();
    }

    public UpdaterPlatform<T> platform() {
        return platform;
    }

    public UpdaterPlugin updaterPlugin() {
        return updaterPlugin;
    }

    public CommandHandler commandPlatform() {
        return commandPlatform;
    }

    public List<PluginDataCollector.Factory> collectors() {
        return collectors;
    }

    public UpdateHandler<T> updateHandler() {
        return updateHandler;
    }

    public ConfigManager config() {
        return config;
    }

    public CompletableFuture<List<PluginData>> collectUnknownPlugins() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, PluginInfo> unknownPlugins = platform.getPlugins().stream()
                .filter(plugin -> config.canRegisterPluginData(plugin.getName()))
                .collect(Collectors.toMap(PluginInfo::getName, plugin -> plugin));
            if (unknownPlugins.isEmpty()) {
                return Collections.emptyList();
            }

            List<PluginDataCollector> collectors = this.collectors.stream()
                .map(factory -> factory.create(this))
                .toList();
            List<PluginData> collectedPluginData = new ArrayList<>();
            for (PluginDataCollector collector : collectors) {
                List<PluginData> foundPluginData;
                try {
                    foundPluginData = collector.collect(unknownPlugins.values());
                } catch (Throwable e) {
                    updaterPlugin.getLogger().log(Level.WARNING, "Caught exception whilst collecting unknown plugin data: ", e);
                    continue;
                }

                collectedPluginData.addAll(foundPluginData);
                for (PluginData pluginData : foundPluginData) {
                    unknownPlugins.remove(pluginData.pluginName());
                }
            }

            return collectedPluginData;
        });
    }

    public @Nullable String constructUpdateMessage() {
        int updatesAvailable = 0;
        for (PluginData pluginData : this.config.getAllPluginData()) {
            if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                updatesAvailable++;
            }
        }

        if (updatesAvailable > 0) {
            return this.config.getMessage("updates-available", "<#e0c01b>%amount% <#ffe27a>updates are available, type <#e0c01b>'%updates_command%' <#ffe27a>for more information!")
                .replace("%amount%", String.valueOf(updatesAvailable))
                .replace("%updates_command%", "/updates list");
        }

        return null;
    }
}
