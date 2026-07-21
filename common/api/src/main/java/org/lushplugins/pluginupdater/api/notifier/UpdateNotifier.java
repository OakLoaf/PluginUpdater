package org.lushplugins.pluginupdater.api.notifier;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pluginupdater.api.version.Version;

import java.util.concurrent.TimeUnit;

public class UpdateNotifier<T> {
    private final Updater<T> updater;
    private final String message;
    private final String permission;

    public UpdateNotifier(Updater<T> updater, String message, @Nullable String permission) {
        this.updater = updater;
        this.message = message;
        this.permission = permission;
    }

    public void notify(T user, @Nullable Integer delay) {
        if (permission == null || updater.platform().hasPermission(user, permission)) {
            PluginData pluginData = updater.pluginData();
            if (pluginData.isUpdateAvailable() && !pluginData.isAlreadyDownloaded()) {
                String message = this.message
                    .replace("%plugin%", pluginData.pluginName())
                    .replace("%current_version%", pluginData.currentVersion().rawVersionString())
                    .replace("%latest_version%", pluginData.latestVersion()
                        .map(Version::rawVersionString)
                        .orElse("unknown"));

                if (delay != null) {
                    updater.scheduler().schedule(() -> updater.platform().sendMessage(user, message), delay, TimeUnit.SECONDS);
                } else {
                    updater.platform().sendMessage(user, message);
                }
            }
        }
    }
}
