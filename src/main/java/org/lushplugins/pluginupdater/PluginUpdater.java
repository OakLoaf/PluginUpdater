package org.lushplugins.pluginupdater;

import org.bukkit.command.CommandExecutor;
import org.lushplugins.lushlib.LushLib;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.command.UpdateCommand;
import org.lushplugins.pluginupdater.command.UpdaterCommand;
import org.lushplugins.pluginupdater.command.UpdatesCommand;
import org.lushplugins.pluginupdater.config.ConfigManager;
import org.lushplugins.pluginupdater.listener.PlayerListener;
import org.lushplugins.pluginupdater.updater.UpdateHandler;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.util.lamp.annotation.PluginName;
import org.lushplugins.pluginupdater.util.lamp.response.StringMessageResponseHandler;
import revxrsal.commands.Lamp;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class PluginUpdater extends JavaPlugin {
    private static PluginUpdater plugin;

    private UpdateHandler updateHandler;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;

        DownloadLogger.setLogFile(new File(plugin.getDataFolder(), "downloads.log"));

        updateHandler = new UpdateHandler();
        updateHandler.enable();

        configManager = new ConfigManager();
        configManager.reloadConfig();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);

        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this)
            .suggestionProviders(providers -> {
                providers.addProviderForAnnotation(PluginName.class, (annotation) -> (context) -> {
                    List<String> plugins = new ArrayList<>();
                    plugins.add("all");
                    plugins.addAll(PluginUpdater.getInstance().getConfigManager().getPlugins());
                    return plugins;
                });
            })
            .responseHandler(String.class, new StringMessageResponseHandler())
            .build();
        lamp.register(new UpdaterCommand(), new UpdatesCommand());

        if (PluginUpdater.getInstance().getConfigManager().shouldAllowDownloads()) {
            lamp.register(new UpdateCommand());
        }
    }

    @Override
    public void onDisable() {
        if (updateHandler != null) {
            updateHandler.shutdown();
            updateHandler = null;
        }

        configManager = null;
        plugin = null;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public static PluginUpdater getInstance() {
        return plugin;
    }
}
