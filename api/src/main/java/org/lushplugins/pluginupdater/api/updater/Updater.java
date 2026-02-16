package org.lushplugins.pluginupdater.api.updater;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.listener.NotificationHandler;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.platform.github.GithubData;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarData;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Updater {
    private final Plugin plugin;
    private final PluginData pluginData;
    private final NotificationHandler notificationHandler;

    private Updater(@NotNull Plugin plugin, @NotNull PluginData pluginData, boolean notify, String notificationPermission, String notificationMessage) {
        this.plugin = plugin;
        this.pluginData = pluginData;
        this.notificationHandler = notify ? new NotificationHandler(this, notificationPermission, notificationMessage) : null;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public PluginData getPluginData() {
        return pluginData;
    }

    public NotificationHandler getNotificationHandler() {
        return notificationHandler;
    }

    /**
     * @return Whether an update is available (Returns false if version has not been checked)
     */
    public boolean isUpdateAvailable() {
        return pluginData.isUpdateAvailable();
    }

    /**
     * @return Whether an update has already been downloaded
     */
    public boolean isAlreadyDownloaded() {
        return pluginData.isAlreadyDownloaded();
    }

    /**
     * Checks if an update for the plugin is available.
     * @return A future containing whether an update is available.
     */
    public CompletableFuture<Boolean> checkForUpdate() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                future.complete(VersionChecker.isUpdateAvailable(pluginData));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
                future.complete(false);
            }
        });

        return future;
    }

    /**
     * Downloads the latest version available version of the plugin.
     * The download will not be attempted if no update is detected as available or if an update has already been
     * downloaded.
     * @return A future containing whether the download was successful.
     */
    public CompletableFuture<Boolean> attemptDownload() {
        if (!pluginData.isEnabled() || !pluginData.isUpdateAvailable() || pluginData.isAlreadyDownloaded()) {
            return CompletableFuture.completedFuture(false);
        }

        return download();
    }

    /**
     * Forcefully downloads the latest version available version of the plugin.
     * @return A future containing whether the download was successful.
     */
    public CompletableFuture<Boolean> forceDownload() {
        return download();
    }

    private CompletableFuture<Boolean> download() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (VersionChecker.download(pluginData)) {
                    pluginData.setVersionDifference(VersionDifference.UNKNOWN);
                    pluginData.setAlreadyDownloaded(true);
                    completableFuture.complete(true);
                } else {
                    completableFuture.complete(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }

    public static Builder builder(Plugin plugin) {
        return new Builder(plugin);
    }

    public static class Builder {
        private final Plugin plugin;
        private final PluginData pluginData;
        private long checkFrequency = 600;
        private boolean notify = true;
        private String notificationPermission = "pluginupdater.notifications";
        private String notificationMessage = "&#ffe27aA new &#e0c01b%plugin% &#ffe27aupdate is now available! &#e0c01b%current_version% &#ffe27a-> &#e0c01b%latest_version%";
        private File downloadLogFile;

        private Builder(Plugin plugin) {
            this.plugin = plugin;
            this.pluginData = PluginData.empty(plugin);
        }

        /**
         * Add GitHub plugin data to be used for collecting update information
         * (Platforms should be added in order of priority).
         * @param repo The plugin's GitHub repo (e.g. 'OakLoaf/PluginUpdater')
         */
        public Builder github(String repo) {
            return platform(new GithubData(repo));
        }

        /**
         * Add Hangar plugin data to be used for collecting update information
         * (Platforms should be added in order of priority).
         * @param projectSlug The plugin's hangar project slug.
         */
        public Builder hangar(String projectSlug) {
            return platform(new HangarData(projectSlug));
        }

        /**
         * Add Modrinth plugin data to be used for collecting update information
         * (Platforms should be added in order of priority).
         * @param projectId The plugin's modrinth project id.
         */
        public Builder modrinth(String projectId) {
            return platform(new ModrinthData(projectId));
        }

        /**
         * Add Spigot plugin data to be used for collecting update information
         * (Platforms should be added in order of priority).
         * @param resourceId The plugin's spigot resource id.
         */
        public Builder spigot(String resourceId) {
            return platform(new SpigotData(resourceId));
        }

        /**
         * Add a plugin's platform data to be used for collecting update information
         * (Platforms should be added in order of priority).
         * @param platformData The platform data.
         */
        public Builder platform(PlatformData platformData) {
            this.pluginData.addPlatform(platformData);
            return this;
        }

        /**
         * Sets whether a version check should be run upon building
         * @param seconds Number of seconds between checks (Default: 600 seconds. Set to -1 to disable)
         */
        public Builder checkSchedule(long seconds) {
            this.checkFrequency = seconds;
            return this;
        }

        /**
         * Sets whether notifications should be sent for this
         * @param shouldSend Whether notifications should be sent.
         */
        public Builder notify(boolean shouldSend) {
            this.notify = shouldSend;
            return this;
        }

        /**
         * Sets the required permission for players to receive update notifications
         * @param permission The permission that players need to receive notifications.
         *                   Defaults to 'pluginupdater.notifications'.
         */
        public Builder notificationPermission(@Nullable String permission) {
            this.notificationPermission = permission;
            return this;
        }

        /**
         * Sets the update notification message.
         * @param message The notification message.
         */
        public Builder notificationMessage(@NotNull String message) {
            this.notificationMessage = message;
            return this;
        }

        /**
         * Sets the log file to log downloads in
         * @param logFile The log file. Defaults to null.
         */
        public Builder logDownloads(@Nullable File logFile) {
            this.downloadLogFile = logFile;
            return this;
        }

        /**
         * Builds and starts the Updater.
         * @return The created Updater instance.
         */
        public Updater build() {
            if (pluginData.getPlatformData().isEmpty()) {
                throw new IllegalStateException("At least 1 platform must be registered before building the Updater.");
            }

            DownloadLogger.setLogFile(downloadLogFile);
            Updater updater = new Updater(plugin, pluginData, notify, notificationPermission, notificationMessage);

            if (checkFrequency > 0) {
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, updater::checkForUpdate, 0, checkFrequency * 20);
            }

            return updater;
        }
    }
}
