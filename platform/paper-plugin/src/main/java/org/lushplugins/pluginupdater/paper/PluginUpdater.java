package org.lushplugins.pluginupdater.paper;

import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.paper.api.PaperUpdaterAPI;
import org.lushplugins.pluginupdater.paper.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PluginUpdater extends JavaPlugin {
    private static PluginUpdater plugin;

    private PaperUpdaterPlatform updater;
    private PaperUpdaterAPI api;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        DownloadLogger.setLogFile(new File(getDataFolder(), "downloads.log"));
        this.updater = new PaperUpdaterPlatform(this);
        this.api = new PaperUpdaterAPI(updater);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        if (updater != null) {
            updater.shutdown();
            updater = null;
        }
    }

    public PaperUpdaterPlatform updater() {
        return updater;
    }

    public PaperUpdaterAPI api() {
        return api;
    }

    public static PluginUpdater getInstance() {
        return plugin;
    }
}
