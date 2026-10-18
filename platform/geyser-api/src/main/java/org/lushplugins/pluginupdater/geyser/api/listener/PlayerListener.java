package org.lushplugins.pluginupdater.geyser.api.listener;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionJoinEvent;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.geyser.api.plugin.GeyserExtensionInfo;

public class PlayerListener {
    private final Updater<GeyserConnection> updater;

    public PlayerListener(Updater<GeyserConnection> updater) {
        this.updater = updater;

        GeyserExtensionInfo pluginInfo = (GeyserExtensionInfo) updater.pluginInfo();
        GeyserApi.api().eventBus().register(pluginInfo.extension(), this);
    }

    @Subscribe
    public void onPlayerJoin(SessionJoinEvent event) {
        updater.notifier().ifPresent(notifier -> notifier.notify(event.connection(), 3));
    }
}
