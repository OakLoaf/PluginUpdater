package org.lushplugins.pluginupdater.api.notifier;

import org.jetbrains.annotations.Nullable;
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

    public void attemptToNotify(T user, @Nullable Integer delay) {
        if (permission == null || hasPermission(user, permission)) {
            PluginData pluginData = updater.getPluginData();
            if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                String message = this.message
                    .replace("%plugin%", pluginData.getPluginName())
                    .replace("%current_version%", pluginData.getCurrentVersion().rawVersionString())
                    .replace("%latest_version%", pluginData.getLatestVersion().rawVersionString());

                if (delay != null) {
                    updater.getScheduler().schedule(() -> sendMessage(user, message), delay, TimeUnit.SECONDS);
                } else {
                    sendMessage(user, message);
                }
            }
        }
    }

    public interface Constructor {
        UpdateNotifier<?> apply(Updater updater, String permission, String message);
    }
}
