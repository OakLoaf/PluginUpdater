package org.lushplugins.pluginupdater.api.updater;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.updater.PluginData;
import org.lushplugins.pluginupdater.updater.VersionChecker;
import org.lushplugins.pluginupdater.updater.VersionDifference;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;

public abstract class Updater {
    private final Plugin plugin;
    private final PluginData pluginData;
    private final VersionChecker versionChecker;

    public Updater(@NotNull Plugin plugin, @NotNull PluginData pluginData, @NotNull VersionChecker versionChecker) {
        this.plugin = plugin;
        this.pluginData = pluginData;
        this.versionChecker = versionChecker;
    }

    public CompletableFuture<Boolean> isUpdateAvailable() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String currentVersion = pluginData.getCurrentVersion();
                Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(versionChecker.getLatestVersion(pluginData));
                if (!matcher.find()) {
                    completableFuture.complete(false);
                    return;
                }
                String latestVersion = matcher.group();

                pluginData.setCheckRan(true);
                VersionDifference versionDifference = VersionChecker.getVersionDifference(currentVersion, latestVersion);
                if (!versionDifference.equals(VersionDifference.LATEST)) {
                    pluginData.setLatestVersion(latestVersion);
                    pluginData.setVersionDifference(versionDifference);
                    completableFuture.complete(true);
                } else {
                    completableFuture.complete(false);
                }
            } catch (IOException | IllegalStateException e) {
                PluginUpdater.getInstance().getLogger().severe(e.getMessage());
                completableFuture.complete(false);
            }
        });

        return completableFuture;
    }

    public CompletableFuture<Boolean> attemptDownload() {
        if (!pluginData.isEnabled() || !pluginData.isUpdateAvailable() || pluginData.isAlreadyDownloaded()) {
            return CompletableFuture.completedFuture(false);
        }

        return download();
    }

    public CompletableFuture<Boolean> forceDownload() {
        return download();
    }

    private CompletableFuture<Boolean> download() {
        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (versionChecker.download(pluginData)) {
                    pluginData.setVersionDifference(VersionDifference.UNKNOWN);
                    pluginData.setAlreadyDownloaded(true);
                    completableFuture.complete(true);
                } else {
                    completableFuture.complete(false);
                }
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        });

        return completableFuture;
    }
}
