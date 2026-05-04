package org.lushplugins.pluginupdater.paper;

import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.CommonPluginCollector;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.paper.api.PaperUpdaterAPI;
import org.lushplugins.pluginupdater.paper.collector.PluginYamlCollector;
import org.lushplugins.pluginupdater.paper.collector.SpigotCollector;
import org.lushplugins.pluginupdater.paper.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.lushplugins.pluginupdater.paper.platform.PaperUpdaterPlatform;

import java.io.File;
import java.util.List;

public final class PaperUpdaterPlugin extends JavaPlugin {
    private static PaperUpdaterPlugin plugin;

    private UpdaterImpl updater;
    private PaperUpdaterAPI api;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        DownloadLogger.setLogFile(new File(getDataFolder(), "downloads.log"));
        PaperUpdaterPlatform platform = new PaperUpdaterPlatform(this);
        this.updater = new UpdaterImpl(platform, List.of(
            CommonPluginCollector::new,
            PluginDataCollector.of(new PluginYamlCollector()),
            PluginDataCollector.of(new ModrinthCollector(platform)),
            PluginDataCollector.of(new SpigotCollector())
        ));
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

    public UpdaterImpl updater() {
        return updater;
    }

    public PaperUpdaterAPI api() {
        return api;
    }

    public static PaperUpdaterPlugin getInstance() {
        return plugin;
    }
}
