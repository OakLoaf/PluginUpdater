package org.lushplugins.pluginupdater.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.common.config.ConfigManager;
import org.lushplugins.pluginupdater.velocity.VelocityUpdaterPlugin;

import java.util.concurrent.TimeUnit;

public class PlayerListener {
    private final VelocityUpdaterPlugin instance;

    public PlayerListener(VelocityUpdaterPlugin instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerJoin(PlayerFinishedConfigurationEvent event) {
        Player player = event.player();
        if (!player.hasPermission("pluginupdater.notify")) {
            return;
        }

        instance.server().getScheduler()
            .buildTask(instance, () -> {
                ConfigManager configManager = instance.updater().config();

                int updatesAvailable = 0;
                for (PluginData pluginData : configManager.getAllPluginData()) {
                    if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                        updatesAvailable++;
                    }
                }

                if (updatesAvailable > 0) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(configManager.getMessage("updates-available", "&#e0c01b%amount% &#ffe27aupdates are available, type &#e0c01b'%updates_command%' &#ffe27afor more information!")
                        .replace("%amount%", String.valueOf(updatesAvailable))
                        .replace("%updates_command%", "/updates list")));
                }
            })
            .delay(5L, TimeUnit.SECONDS)
            .schedule();
    }
}
