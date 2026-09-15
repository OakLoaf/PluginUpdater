package org.lushplugins.pluginupdater.geyser.listener;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionJoinEvent;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.geyser.GeyserUpdaterExtension;
import org.lushplugins.pluginupdater.geyser.api.platform.GeyserUpdaterPlatform;

import java.util.concurrent.TimeUnit;

public class PlayerListener {
    private final GeyserUpdaterExtension instance;

    public PlayerListener(GeyserUpdaterExtension instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerJoin(SessionJoinEvent event) {
        GeyserConnection player = event.connection();
        if (!player.hasPermission("pluginupdater.notify")) {
            return;
        }

        UpdaterImpl<?> updater = instance.updater();
        if (updater.updateHandler().remainingWithState(UpdateHandler.ProcessingData.State.SEND_NOTIFICATION) == 0) {
            String message = updater.constructUpdateMessage();
            if (message != null) {
                GeyserUpdaterPlatform platform = (GeyserUpdaterPlatform) updater.platform();
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
