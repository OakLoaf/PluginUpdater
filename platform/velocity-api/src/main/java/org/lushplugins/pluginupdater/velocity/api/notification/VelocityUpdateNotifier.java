package org.lushplugins.pluginupdater.velocity.api.notification;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.lushplugins.pluginupdater.api.listener.UpdateNotifier;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.velocity.api.plugin.VelocityPluginInfo;

public class VelocityUpdateNotifier extends UpdateNotifier<Player> {

    public VelocityUpdateNotifier(ProxyServer server, Updater updater, String message, String permission) {
        super(updater, message, permission);

        VelocityPluginInfo pluginInfo = (VelocityPluginInfo) updater.getPluginInfo();
        server.getEventManager().register(this, pluginInfo.plugin());
    }

    @Override
    public boolean hasPermission(Player user, String permission) {
        return user.hasPermission(permission);
    }

    @Override
    public void sendMessage(Player user, String message) {
        user.sendMessage(MiniMessage.miniMessage().deserialize(message));
    }

    @Subscribe
    public void onPlayerJoin(PlayerFinishedConfigurationEvent event) {
        handle(event.player());
    }
}
