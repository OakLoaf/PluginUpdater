package org.lushplugins.pluginupdater.api.listener;

import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.Updater;

import java.util.concurrent.TimeUnit;

public abstract class UpdateNotifier<T> {
    private final Updater updater;
    private final String message;
    private final String permission;

    public UpdateNotifier(Updater updater, String message, String permission) {
        this.updater = updater;
        this.message = message;
        this.permission = permission;
    }

    public abstract boolean hasPermission(T user, String permission);

    public abstract void sendMessage(T user, String message);

    public void handle(T user) {
        if (permission == null || hasPermission(user, permission)) {
            PluginData pluginData = updater.getPluginData();
            if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                String message = this.message
                    .replace("%plugin%", pluginData.getPluginName())
                    .replace("%current_version%", pluginData.getCurrentVersion())
                    .replace("%latest_version%", pluginData.getLatestVersion());

                updater.getScheduler().schedule(() -> sendMessage(user, message), 3, TimeUnit.SECONDS);
            }
        }
    }

    public interface Constructor {
        UpdateNotifier<?> apply(Updater updater, String permission, String message);
    }
}
