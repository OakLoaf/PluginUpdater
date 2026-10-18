package org.lushplugins.pluginupdater.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.CommonPluginCollector;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;
import org.lushplugins.pluginupdater.util.BuildParameters;
import org.lushplugins.pluginupdater.velocity.api.VelocityUpdater;
import org.lushplugins.pluginupdater.velocity.api.VelocityUpdaterAPI;
import org.lushplugins.pluginupdater.velocity.api.platform.VelocityUpdaterPlatform;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;
import org.lushplugins.pluginupdater.velocity.api.util.VelocityUtil;
import org.lushplugins.pluginupdater.velocity.command.VelocityCommandHandler;
import org.lushplugins.pluginupdater.velocity.listener.PlayerListener;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

@Plugin(
    id = "pluginupdater",
    name = "PluginUpdater",
    version = BuildParameters.VERSION + "+" + BuildParameters.COMMIT
)
public class VelocityUpdaterPlugin implements UpdaterPlugin {
    private static VelocityUpdaterPlugin instance;

    private final ProxyServer server;
    private final ComponentLogger logger;
    private final Path dataFolder;
    private UpdaterImpl<Player> updater;
    private VelocityUpdaterAPI api;

    @Inject
    public VelocityUpdaterPlugin(ProxyServer server, ComponentLogger logger, @DataDirectory Path dataFolder) {
        UpdaterConstants.LOGGER = logger;
        VelocityUpdater.populateRegistries(server);
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        DownloadLogger.setLogFile(dataFolder.resolve("downloads.log").toFile());
        VelocityUpdaterPlatform platform = new VelocityUpdaterPlatform(server, logger);
        this.updater = new UpdaterImpl<>(
            platform,
            this,
            new VelocityCommandHandler(this),
            List.of(
                CommonPluginCollector::new,
                ModrinthCollector::new
            )
        );
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

    public UpdaterImpl<Player> updater() {
        return updater;
    }

    public VelocityUpdaterAPI api() {
        return api;
    }

    @Override
    public Path getDataPath() {
        return dataFolder;
    }

    @Override
    public Path getDownloadDir() {
        return VelocityUtil.getUpdateFolder().orElse(null);
    }

    @Override
    public InputStream getResourceStream(String path) {
        return this.getClass().getResourceAsStream("/" + path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        PluginContainer container = ((VelocityPluginInfo) pluginInfo).container();
        return container.getInstance().orElseThrow().getClass().getResourceAsStream(path);
    }

    @Override
    public ComponentLogger getComponentLogger() {
        return logger;
    }

    public static VelocityUpdaterPlugin getInstance() {
        return instance;
    }
}
