package org.lushplugins.pluginupdater;

import org.bukkit.command.CommandExecutor;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.command.UpdateCommand;
import org.lushplugins.pluginupdater.command.UpdaterCommand;
import org.lushplugins.pluginupdater.command.UpdatesCommand;
import org.lushplugins.pluginupdater.config.ConfigManager;
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

    public void registerCommand(Command command) {
        registerCommand(command.getName(), command);
    }

    public void registerCommand(String command, CommandExecutor executor) {
        try {
            getCommand(command).setExecutor(executor);
        } catch (NullPointerException e) {
            getLogger().severe("Failed to register command '" + command + "', make sure the command has been defined in the plugin.yml");
            e.printStackTrace();
        }
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
