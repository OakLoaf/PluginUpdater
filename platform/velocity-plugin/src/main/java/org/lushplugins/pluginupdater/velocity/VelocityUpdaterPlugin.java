package org.lushplugins.pluginupdater.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.CommonPluginCollector;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.collector.PluginDataCollector;
import org.lushplugins.pluginupdater.util.BuildParameters;
import org.lushplugins.pluginupdater.velocity.api.VelocityUpdaterAPI;
import org.lushplugins.pluginupdater.velocity.listener.PlayerListener;
import org.lushplugins.pluginupdater.velocity.platform.VelocityUpdaterPlatform;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

@Plugin(
    id = "pluginupdater",
    name = "PluginUpdater",
    version = BuildParameters.VERSION
)
public class VelocityUpdaterPlugin {
    private static VelocityUpdaterPlugin instance;

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataFolder;
    private UpdaterImpl updater;
    private VelocityUpdaterAPI api;

    @Inject
    public VelocityUpdaterPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        DownloadLogger.setLogFile(dataFolder.resolve("downloads.log").toFile());
        VelocityUpdaterPlatform platform = new VelocityUpdaterPlatform(this);
        this.updater = new UpdaterImpl(platform, List.of(
            CommonPluginCollector::new,
            PluginDataCollector.of(new ModrinthCollector(platform))
        ));
        this.api = new VelocityUpdaterAPI(updater);

        server.getEventManager().register(this, new PlayerListener(this));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (updater != null) {
            updater.shutdown();
            updater = null;
        }
    }

    public ProxyServer server() {
        return server;
    }

    public Logger logger() {
        return logger;
    }

    public Path dataFolder() {
        return dataFolder;
    }

    public UpdaterImpl updater() {
        return updater;
    }

    public  VelocityUpdaterAPI api() {
        return api;
    }

    public static VelocityUpdaterPlugin getInstance() {
        return instance;
    }
}
