package org.lushplugins.pluginupdater.common;

import org.lushplugins.pluginupdater.api.source.SourceRegistry;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.common.command.annotation.CommandPermissionFactory;
import org.lushplugins.pluginupdater.common.command.annotation.PluginName;
import org.lushplugins.pluginupdater.common.command.response.StringMessageResponseHandler;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import revxrsal.commands.Lamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UpdaterImpl {
    private final UpdaterPlatform platform;
    private final List<PluginDataCollector.Factory> collectors;
    private final UpdateHandler updateHandler;
    private final ConfigManager config;

    public UpdaterImpl(UpdaterPlatform platform, List<PluginDataCollector.Factory> collectors) {
        this.platform = platform;
        this.collectors = collectors;

        updateHandler = new UpdateHandler(this);
        updateHandler.enable();

        config = new ConfigManager(this);
        config.reload();

        Lamp<?> lamp = platform.prepareLamp()
            .permissionFactory(new CommandPermissionFactory(this))
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

                    return config.getPlugins();
                })
            )
            .responseHandler(String.class, new StringMessageResponseHandler())
            .build();

        platform.registerLampCommands(this, lamp);
    }

    public void shutdown() {
        updateHandler.shutdown();
    }

    public UpdaterPlatform platform() {
        return platform;
    }

    public List<PluginDataCollector.Factory> collectors() {
        return collectors;
    }

    public UpdateHandler updateHandler() {
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
                List<PluginData> foundPluginData = collector.collect(unknownPlugins.values());

                collectedPluginData.addAll(foundPluginData);
                for (PluginData pluginData : foundPluginData) {
                    unknownPlugins.remove(pluginData.getPluginName());
                }
            }

            return collectedPluginData;
        });
    }
}
