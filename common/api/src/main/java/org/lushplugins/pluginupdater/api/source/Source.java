package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.*;
import java.util.logging.Level;

@SuppressWarnings("CodeBlock2Expr")
public interface Source {

    String getName();

    Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException, InvalidVersionFormatException;

    DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException;

    default boolean isUpdateAvailable(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        Version currentVersion = pluginData.getCurrentVersion();
        Version latestVersion;
        try {
            latestVersion = getLatestVersion(pluginData, sourceData);
        } catch (InvalidVersionFormatException e) {
            UpdaterConstants.LOGGER.severe("Failed to read latest version for '%s': %s".formatted(pluginData.getPluginName(), e.getMessage()));
            return false;
        }

        VersionDifference versionDifference;
        try {
            VersionComparator comparator = pluginData.getOptionalComparator().orElse(sourceData.getDefaultComparator());
            versionDifference = comparator.getVersionDifference(currentVersion, latestVersion);
        } catch (InvalidVersionFormatException e) {
            UpdaterConstants.LOGGER.severe("Failed to compare versions for '%s': %s".formatted(pluginData.getPluginName(), e.getMessage()));
            return false;
        }

        pluginData.setCheckRan(true);

        if (!versionDifference.equals(VersionDifference.LATEST)) {
            pluginData.setLatestVersion(latestVersion);
            pluginData.setVersionDifference(versionDifference);
            return true;
        } else {
            return false;
        }
    }

    default boolean download(PluginData pluginData, SourceData sourceData, File destinationDir) throws IOException, InterruptedException {
        DownloadableRelease release = getDownloadableRelease(pluginData, sourceData);
        if (release == null) {
            return false;
        }

        String fallbackFileName = pluginData.getPluginName() + "-" + pluginData.getLatestVersion().version() + ".jar";
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

    static Version getLatestVersion(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.getLatestVersion(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to check plugin '" + pluginData.getPluginName() + "' for latest version.", e);
        }
    }

    static DownloadableRelease getDownloadableRelease(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (source, sourceData) -> {
                return source.getDownloadableRelease(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to get download url for plugin '" + pluginData.getPluginName() + "'.", e);
        }
    }

    static boolean isUpdateAvailable(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.isUpdateAvailable(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to check if update is available for plugin '" + pluginData.getPluginName() + "'.", e);
        }
    }

    static boolean download(PluginData pluginData, File destinationDir) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.download(pluginData, sourceData, destinationDir);
            });
        } catch (IOException e) {
            throw new IOException("Failed to download update for plugin '" + pluginData.getPluginName() + "'.", e);
        }
    }

    private static <T> T attemptOnSources(PluginData pluginData, SourceSupplier<T> callable) throws IOException {
        for (SourceData sourceData : pluginData.getSourceData()) {
            Source source = SourceRegistry.get(sourceData.sourceName());
            if (source == null) {
                continue;
            }

            try {
                return callable.call(source, sourceData);
            } catch (IOException | InterruptedException e) {
                UpdaterConstants.LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        throw new IOException("Failed attempts on all available sources for plugin '" + pluginData.getPluginName() + "'.");
    }

    @FunctionalInterface
    interface SourceSupplier<T> {
        T call(Source source, SourceData sourceData) throws IOException, InterruptedException;
    }
}
