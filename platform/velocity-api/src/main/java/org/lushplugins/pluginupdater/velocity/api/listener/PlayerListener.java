package org.lushplugins.pluginupdater.velocity.api.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;

public class PlayerListener {
    private final Updater<Player> updater;

    public PlayerListener(Updater<Player> updater, ProxyServer server) {
        this.updater = updater;

        VelocityPluginInfo pluginInfo = (VelocityPluginInfo) updater.pluginInfo();
        server.getEventManager().register(this, pluginInfo.container());
    }

    @Subscribe
    public void onPlayerJoin(PlayerFinishedConfigurationEvent event) {
        updater.notifier().ifPresent(notifier -> notifier.notify(event.player(), 3));
    }
}
