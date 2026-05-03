package org.lushplugins.pluginupdater.paper;

import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.paper.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PluginUpdater extends JavaPlugin {
    private static PluginUpdater plugin;

    private PaperUpdaterPlatform updater;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        DownloadLogger.setLogFile(new File(getDataFolder(), "downloads.log"));
        updater = new PaperUpdaterPlatform(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        if (updater != null) {
            updater.shutdown();
            updater = null;
        }
    }

    public PaperUpdaterPlatform getUpdater() {
        return updater;
    }

    public static PluginUpdater getInstance() {
        return plugin;
    }
}
