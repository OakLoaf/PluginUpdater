package me.oak.pluginupdater;

import me.oak.pluginupdater.command.PluginUpdaterCommand;
import me.oak.pluginupdater.command.PluginUpdatesCommand;
import me.oak.pluginupdater.config.ConfigManager;
import me.oak.pluginupdater.updater.UpdateHandler;
import me.oak.pluginupdater.updater.platform.PlatformRegistry;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class PluginUpdater extends JavaPlugin {
    private static PluginUpdater plugin;
    private PlatformRegistry platformRegistry;
    private UpdateHandler updateHandler;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        plugin = this;
        platformRegistry = new PlatformRegistry();

        updateHandler = new UpdateHandler();
        updateHandler.enable();

        configManager = new ConfigManager();
        configManager.reloadConfig();
        if (configManager.shouldCheckOnStartup()) {
            configManager.getPlugins().forEach(pluginName -> updateHandler.queueUpdateCheck(pluginName));
        }

        getCommand("pluginupdater").setExecutor(new PluginUpdaterCommand());
        getCommand("pluginupdates").setExecutor(new PluginUpdatesCommand());
    }

    @Override
    public void onDisable() {
        if (updateHandler != null) {
            updateHandler.shutdown();
            updateHandler = null;
        }

        platformRegistry = null;
        configManager = null;
        plugin = null;
    }

    public PlatformRegistry getUpdaterRegistry() {
        return platformRegistry;
    }

    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public File getUpdateFolder() {
        File updateDir = new File(PluginUpdater.getInstance().getDataFolder().getParentFile(), Bukkit.getUpdateFolder());

        if (!updateDir.exists()) {
            updateDir.mkdir();
        }

        return updateDir;
    }

    public static PluginUpdater getInstance() {
        return plugin;
    }
}
