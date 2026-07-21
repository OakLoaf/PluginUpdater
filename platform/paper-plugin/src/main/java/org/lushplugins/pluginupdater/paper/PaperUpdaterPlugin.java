package org.lushplugins.pluginupdater.paper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.CommonPluginCollector;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;
import org.lushplugins.pluginupdater.paper.api.PaperUpdaterAPI;
import org.lushplugins.pluginupdater.paper.api.platform.PaperUpdaterPlatform;
import org.lushplugins.pluginupdater.paper.api.plugin.PaperPluginInfo;
import org.lushplugins.pluginupdater.paper.collector.PluginYamlCollector;
import org.lushplugins.pluginupdater.paper.collector.SpigotCollector;
import org.lushplugins.pluginupdater.paper.command.PaperCommandHandler;
import org.lushplugins.pluginupdater.paper.listener.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public final class PaperUpdaterPlugin extends JavaPlugin implements UpdaterPlugin {
    private static PaperUpdaterPlugin plugin;

    private UpdaterImpl<Player> updater;
    private PaperUpdaterAPI api;

    @Override
    public void onLoad() {
        plugin = this;
    }

    @Override
    public void onEnable() {
        DownloadLogger.setLogFile(new File(getDataFolder(), "downloads.log"));
        PaperUpdaterPlatform platform = new PaperUpdaterPlatform();
        this.updater = new UpdaterImpl<>(
            platform,
            this,
            new PaperCommandHandler(),
            List.of(
                CommonPluginCollector::new,
                PluginYamlCollector::new,
                ModrinthCollector::new,
                SpigotCollector::new
            )
        );
        this.api = new PaperUpdaterAPI(updater);

        getServer().getPluginManager().registerEvents(new PlayerListener(platform), this);
    }

    @Override
    public void onDisable() {
        if (updater != null) {
            updater.shutdown();
            updater = null;
        }
    }

    public UpdaterImpl<Player> updater() {
        return updater;
    }

    public PaperUpdaterAPI api() {
        return api;
    }

    @Override
    public @NonNull Path getDataPath() {
        return super.getDataPath();
    }

    @Override
    public Path getDownloadDir() {
        return Bukkit.getUpdateFolderFile().toPath();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return plugin.getResource(path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        return ((PaperPluginInfo) pluginInfo).plugin().getResource(path);
    }

    public static PaperUpdaterPlugin getInstance() {
        return plugin;
    }
}
