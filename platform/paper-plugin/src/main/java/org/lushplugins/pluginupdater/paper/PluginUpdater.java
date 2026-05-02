package org.lushplugins.pluginupdater.paper;

import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.paper.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class PluginUpdater extends JavaPlugin {
    private static PluginUpdater plugin;

    private PaperUpdaterImpl updaterImpl;

    @Override
    public void onEnable() {
        plugin = this;

        DownloadLogger.setLogFile(new File(plugin.getDataFolder(), "downloads.log"));

        updaterImpl = new PaperUpdaterImpl(this);

        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
    }

    @Override
    public void onDisable() {
        if (updaterImpl != null) {
            updaterImpl.shutdown();
            updaterImpl = null;
        }
    }

    public PaperUpdaterImpl getUpdaterImpl() {
        return updaterImpl;
    }

    public static PluginUpdater getInstance() {
        return plugin;
    }
}
