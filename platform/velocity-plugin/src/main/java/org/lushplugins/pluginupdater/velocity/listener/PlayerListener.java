package org.lushplugins.pluginupdater.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.configuration.PlayerFinishedConfigurationEvent;
import com.velocitypowered.api.proxy.Player;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.velocity.VelocityUpdaterPlugin;
import org.lushplugins.pluginupdater.velocity.platform.VelocityUpdaterPlatform;

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

        UpdaterImpl updater = instance.updater();
        if (updater.updateHandler().remainingWithState(UpdateHandler.ProcessingData.State.SEND_NOTIFICATION) == 0) {
            String message = updater.constructUpdateMessage();
            if (message != null) {
                VelocityUpdaterPlatform platform = (VelocityUpdaterPlatform) updater.platform();
                instance.server().getScheduler()
                    .buildTask(instance, () -> {
                        platform.sendMessage(player, updater.constructUpdateMessage());
                    })
                    .delay(5L, TimeUnit.SECONDS)
                    .schedule();
            }
        }
    }
}
