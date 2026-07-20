package org.lushplugins.pluginupdater.api.updater;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.notifier.UpdateNotifier;
import org.lushplugins.pluginupdater.api.platform.UpdaterPlatform;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.source.type.*;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Updater<T> {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final UpdaterPlatform<T> platform;
    private final PluginInfo plugin;
    private final PluginData pluginData;
    private final Path downloadDir;
    private final UpdateNotifier<T> notifier;

    @Contract("_, _, _, _, true, null, _ -> fail; ")
    private Updater(
        UpdaterPlatform<T> platform,
        PluginInfo plugin,
        PluginData pluginData,
        @Nullable Path downloadDir,
        boolean notify,
        String notificationMessage,
        String notificationPermission
    ) {
        this.platform = platform;
        this.plugin = plugin;
        this.pluginData = pluginData;
        this.downloadDir = downloadDir;
        this.notifier = notify ? new UpdateNotifier<>(this, notificationMessage, notificationPermission) : null;
    }

    public ScheduledExecutorService scheduler() {
        return scheduler;
    }

    public UpdaterPlatform<T> platform() {
        return platform;
    }

    public PluginInfo pluginInfo() {
        return plugin;
    }

    public PluginData pluginData() {
        return pluginData;
    }

    public Optional<Path> downloadDir() {
        return Optional.ofNullable(downloadDir);
    }

    public Optional<UpdateNotifier<T>> notifier() {
        return Optional.ofNullable(notifier);
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
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Source.isUpdateAvailable(pluginData);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
                return false;
            }
        });
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
        Objects.requireNonNull(downloadDir, "downloadDir cannot be null");

        return CompletableFuture.supplyAsync(() -> {
            try {
                boolean success = Source.download(pluginData, downloadDir);

                if (success) {
                    pluginData.versionDifference(VersionDifference.UNKNOWN);
                    pluginData.setAlreadyDownloaded(true);
                }

                return success;
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, e.getMessage(), e);
                return false;
            }
        });
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    public static <T> Builder<T> builder(UpdaterPlatform<T> platform, PluginInfo plugin) {
        return new Builder<>(platform, plugin);
    }

    public static class Builder<T> {
        private final UpdaterPlatform<T> platform;
        private final PluginInfo plugin;
        private final PluginData pluginData;
        private Path downloadDir;
        private long checkFrequency = 600;
        private boolean notify = true;
        private String notificationPermission = "pluginupdater.notifications";
        private String notificationMessage = "<#ffe27>A new <#e0c01b>%plugin% <#ffe27>update is now available! <#e0c01b>%current_version% <#ffe27a>-> <#e0c01b>%latest_version%";
        private File downloadLogFile;
        private Consumer<Updater<T>> postBuild;

        private Builder(UpdaterPlatform<T> platform, PluginInfo plugin) {
            this.platform = platform;
            this.plugin = plugin;
            this.pluginData = PluginData.of(plugin);
        }

        public Builder<T> downloadDir(Path downloadDir) {
            this.downloadDir = downloadDir;
            return this;
        }

        /**
         * Add a plugin's source data to be used for collecting update information
         * (Sources should be added in order of priority).
         * @param sourceData The source data.
         */
        public Builder<T> source(SourceData sourceData) {
            this.pluginData.addSource(sourceData);
            return this;
        }

        /**
         * Add Geyser source data to be used for collecting update information
         * (Sources should be added in order of priority).
         **/
        public Builder<T> geyser(UnaryOperator<GeyserSource.Data.Builder> builder) {
            return source(builder.apply(GeyserSource.Data.builder()).build());
        }

        /**
         * Add GitHub source data to be used for collecting update information
         * (Sources should be added in order of priority).
         **/
        public Builder<T> github(UnaryOperator<GithubSource.Data.Builder> builder) {
            return source(builder.apply(GithubSource.Data.builder()).build());
        }

        /**
         * Add Hangar source data to be used for collecting update information
         * (Sources should be added in order of priority).
         **/
        public Builder<T> hangar(UnaryOperator<HangarSource.Data.Builder> builder) {
            return source(builder.apply(HangarSource.Data.builder()).build());
        }

        /**
         * Add Jenkins source data to be used for collecting update information
         * (Sources should be added in order of priority).
         **/
        public Builder<T> jenkins(UnaryOperator<JenkinsSource.Data.Builder> builder) {
            return source(builder.apply(JenkinsSource.Data.builder()).build());
        }

        /**
         * Add Modrinth source data to be used for collecting update information
         * (Sources should be added in order of priority).
         **/
        public Builder<T> modrinth(UnaryOperator<ModrinthSource.Data.Builder> builder) {
            return source(builder.apply(ModrinthSource.Data.builder()).build());
        }

        /**
         * Add Spigot source data to be used for collecting update information
         * (Sources should be added in order of priority).
         **/
        public Builder<T> spigot(UnaryOperator<SpigotSource.Data.Builder> builder) {
            return source(builder.apply(SpigotSource.Data.builder()).build());
        }

        /**
         * Sets whether a version check should be run upon building
         * @param seconds Number of seconds between checks (Default: 600 seconds. Set to -1 to disable)
         */
        public Builder<T> checkSchedule(long seconds) {
            this.checkFrequency = seconds;
            return this;
        }

        /**
         * Sets whether notifications should be sent for this
         * @param shouldSend Whether notifications should be sent.
         */
        public Builder<T> notify(boolean shouldSend) {
            this.notify = shouldSend;
            return this;
        }

        /**
         * Sets the update notification message.
         * @param message The notification message.
         */
        public Builder<T> notificationMessage(String message) {
            this.notificationMessage = message;
            return this;
        }

        /**
         * Sets the required permission for players to receive update notifications
         * @param permission The permission that players need to receive notifications.
         *                   Defaults to {@code pluginupdater.notifications}
         */
        public Builder<T> notificationPermission(@Nullable String permission) {
            this.notificationPermission = permission;
            return this;
        }

        /**
         * Sets the log file to log downloads in
         * @param logFile The log file. Defaults to null.
         */
        public Builder<T> logDownloads(@Nullable File logFile) {
            this.downloadLogFile = logFile;
            return this;
        }

        /**
         * Define a task to be run after the {@link Updater} has been built
         * @param consumer The task to run after building the {@link Updater}
         */
        public Builder<T> onBuild(Consumer<Updater<T>> consumer) {
            this.postBuild = consumer;
            return this;
        }

        /**
         * Builds and starts the Updater.
         * @return The created Updater instance.
         */
        public Updater<T> build() {
            if (pluginData.sourceData().isEmpty()) {
                throw new IllegalStateException("At least 1 source must be registered before building the Updater.");
            }

            DownloadLogger.setLogFile(downloadLogFile);
            Updater<T> updater = new Updater<>(
                platform,
                plugin,
                pluginData,
                downloadDir,
                notify,
                Objects.requireNonNull(notificationMessage),
                notificationPermission
            );

            if (checkFrequency > 0) {
                updater.scheduler().scheduleAtFixedRate(updater::checkForUpdate, 0, checkFrequency, TimeUnit.SECONDS);
            }

            postBuild.accept(updater);

            return updater;
        }
    }
}
