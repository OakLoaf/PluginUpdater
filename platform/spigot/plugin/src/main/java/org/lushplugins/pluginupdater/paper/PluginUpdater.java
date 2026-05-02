package org.lushplugins.pluginupdater.paper;

import org.lushplugins.pluginupdater.paper.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.paper.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PluginUpdater extends JavaPlugin {
    private static PluginUpdater plugin;

    private SpigotUpdaterImpl updaterImpl;

    @Override
    public void onEnable() {
        plugin = this;

        DownloadLogger.setLogFile(new File(plugin.getDataFolder(), "downloads.log"));

        updaterImpl = new SpigotUpdaterImpl();

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        if (updaterImpl != null) {
            updaterImpl.shutdown();
            updaterImpl = null;
        }
    }

    public SpigotUpdaterImpl getUpdaterImpl() {
        return updaterImpl;
    }

    public static PluginUpdater getInstance() {
        return plugin;
    }
}
