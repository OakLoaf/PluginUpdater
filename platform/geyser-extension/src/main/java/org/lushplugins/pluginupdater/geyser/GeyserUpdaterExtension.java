package org.lushplugins.pluginupdater.geyser;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserShutdownEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.collector.CommonPluginCollector;
import org.lushplugins.pluginupdater.common.collector.ModrinthCollector;
import org.lushplugins.pluginupdater.common.platform.UpdaterPlugin;
import org.lushplugins.pluginupdater.geyser.api.GeyserUpdater;
import org.lushplugins.pluginupdater.geyser.api.GeyserUpdaterAPI;
import org.lushplugins.pluginupdater.geyser.api.platform.GeyserUpdaterPlatform;
import org.lushplugins.pluginupdater.geyser.api.plugin.GeyserExtensionInfo;
import org.lushplugins.pluginupdater.geyser.api.util.GeyserUtil;
import org.lushplugins.pluginupdater.geyser.command.GeyserCommandHandler;
import org.lushplugins.pluginupdater.geyser.listener.PlayerListener;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class GeyserUpdaterExtension implements UpdaterPlugin, Extension {
    private static GeyserUpdaterExtension instance;

    private UpdaterImpl<GeyserConnection> updater;
    private GeyserUpdaterAPI api;

    public GeyserUpdaterExtension() {
        GeyserUpdater.populateRegistries();
        instance = this;
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        DownloadLogger.setLogFile(dataFolder().resolve("downloads.log").toFile());
        GeyserUpdaterPlatform platform = new GeyserUpdaterPlatform(getComponentLogger());
        this.updater = new UpdaterImpl<>(
            platform,
            this,
            new GeyserCommandHandler(this),
            List.of(
                CommonPluginCollector::new,
                ModrinthCollector::new
            )
        );
        this.api = new GeyserUpdaterAPI(updater);

        // TODO: Check whether we need to register this class as a listener
        GeyserApi.api().eventBus().register(this, new PlayerListener(this));
    }

    @Subscribe
    public void onProxyShutdown(GeyserShutdownEvent event) {
        if (updater != null) {
            updater.shutdown();
            updater = null;
        }
    }

    public UpdaterImpl<GeyserConnection> updater() {
        return updater;
    }

    public GeyserUpdaterAPI api() {
        return api;
    }

    @Override
    public Path getDataPath() {
        return dataFolder();
    }

    @Override
    public Path getDownloadDir() {
        return GeyserUtil.getUpdateFolder();
    }

    @Override
    public InputStream getResourceStream(String path) {
        return this.getClass().getResourceAsStream("/" + path);
    }

    @Override
    public InputStream getResourceStream(PluginInfo pluginInfo, String path) {
        Extension extension = ((GeyserExtensionInfo) pluginInfo).extension();
        return extension.getClass().getResourceAsStream(path);
    }

    @Override
    public ComponentLogger getComponentLogger() {
        return ComponentLogger.logger(logger().prefix());
    }

    public static GeyserUpdaterExtension getInstance() {
        return instance;
    }
}
