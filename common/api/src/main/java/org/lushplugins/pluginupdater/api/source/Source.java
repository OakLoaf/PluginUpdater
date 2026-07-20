package org.lushplugins.pluginupdater.api.source;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.*;
import java.nio.file.Path;
import java.util.logging.Level;

@SuppressWarnings("CodeBlock2Expr")
public interface Source {

    String getName();

    Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException, InvalidVersionFormatException;

    DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException;

    @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData);

    default boolean isUpdateAvailable(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        Version currentVersion = pluginData.currentVersion();
        Version latestVersion;
        try {
            latestVersion = getLatestVersion(pluginData, sourceData);
        } catch (InvalidVersionFormatException e) {
            UpdaterConstants.LOGGER.severe("Failed to read latest version for '%s': %s".formatted(pluginData.pluginName(), e.getMessage()));
            return false;
        }

        VersionDifference versionDifference;
        try {
            VersionComparator comparator = pluginData.versionComparator().orElse(sourceData.defaultComparator());
            versionDifference = comparator.compare(currentVersion, latestVersion);
        } catch (InvalidVersionFormatException e) {
            UpdaterConstants.LOGGER.severe("Failed to compare versions for '%s': %s".formatted(pluginData.pluginName(), e.getMessage()));
            return false;
        }

        pluginData.setCheckRan(true);
        pluginData.versionDifference(versionDifference);

        if (!versionDifference.equals(VersionDifference.LATEST)) {
            pluginData.latestVersion(latestVersion);
            return true;
        } else {
            return false;
        }
    }

    default boolean download(PluginData pluginData, SourceData sourceData, Path destinationDir) throws IOException, InterruptedException {
        DownloadableRelease release = getDownloadableRelease(pluginData, sourceData);
        if (release == null) {
            return false;
        }

        String version = pluginData.latestVersion().orElseThrow().resolvedVersion();
        String fallbackFileName = pluginData.pluginName() + "-" + version + ".jar";
        release.downloadTo(destinationDir, fallbackFileName);
        DownloadLogger.logDownload(pluginData);

        return true;
    }

    /**
     * @return The endpoint's rate limit per second or {@code -1} if there is no limit
     */
    default int getRateLimit() {
        return -1;
    }

    // TODO: Refactor static methods into a more suitable class
    static Version getLatestVersion(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (source, sourceData) -> {
                return source.getLatestVersion(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to check plugin '" + pluginData.pluginName() + "' for latest version.", e);
        }
    }

    static DownloadableRelease getDownloadableRelease(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (source, sourceData) -> {
                return source.getDownloadableRelease(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to get download url for plugin '" + pluginData.pluginName() + "'.", e);
        }
    }

    static boolean isUpdateAvailable(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (source, sourceData) -> {
                return source.isUpdateAvailable(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to check if update is available for plugin '" + pluginData.pluginName() + "'.", e);
        }
    }

    static boolean download(PluginData pluginData, Path destinationDir) throws IOException {
        try {
            return attemptOnSources(pluginData, (source, sourceData) -> {
                return source.download(pluginData, sourceData, destinationDir);
            });
        } catch (IOException e) {
            throw new IOException("Failed to download update for plugin '" + pluginData.pluginName() + "'.", e);
        }
    }

    private static <T> T attemptOnSources(PluginData pluginData, SourceSupplier<T> callable) throws IOException {
        for (SourceData sourceData : pluginData.sourceData()) {
            Source source = SourceRegistry.get(sourceData.sourceName()).orElse(null);
            if (source == null) {
                continue;
            }

            try {
                return callable.call(source, sourceData);
            } catch (Throwable e) {
                UpdaterConstants.LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        throw new IOException("Failed attempts on all available sources for plugin '" + pluginData.pluginName() + "'.");
    }

    @FunctionalInterface
    interface SourceSupplier<T> {
        T call(Source source, SourceData sourceData) throws IOException, InterruptedException;
    }
}
