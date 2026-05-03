package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings("CodeBlock2Expr")
public interface Source {

    String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException;

    String getDownloadUrl(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException;

    default Map<String, String> getDownloadHeaders(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        return Collections.emptyMap();
    }

    default boolean isUpdateAvailable(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        String currentVersion = pluginData.getCurrentVersion();
        String latestVersion = getLatestVersion(pluginData, sourceData);

        VersionComparator comparator = pluginData.getOptionalComparator().orElse(sourceData.getDefaultComparator());
        VersionDifference versionDifference;
        try {
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
        String pluginName = pluginData.getPluginName();
        String latestVersion = pluginData.getLatestVersion();
        String downloadUrl = getDownloadUrl(pluginData, sourceData);
        if (downloadUrl == null) {
            return false;
        }

        URL url = URI.create(downloadUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + UpdaterConstants.VERSION);
        for (Map.Entry<String, String> header : getDownloadHeaders(pluginData, sourceData).entrySet()) {
            connection.addRequestProperty(header.getKey(), header.getValue());
        }
        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Response code was " + connection.getResponseCode());
        }

        // Get file name or default to PluginName-Version.jar
        String fileName = url.getFile();
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("\\")) {
            fileName = pluginName + "-" + latestVersion + ".jar";
        }

        // Ensures update folder exists
        destinationDir.mkdirs();

        // Downloads file from url
        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        File out = new File(destinationDir, fileName);
        UpdaterConstants.LOGGER.info("Saving '" + fileName + "' to '" + out.getAbsolutePath() + "'");
        FileOutputStream fos = new FileOutputStream(out);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();

        DownloadLogger.logDownload(pluginData);

        return true;
    }

    /**
     * @return The endpoint's rate limit per second or {@code -1} if there is no limit
     */
    default int getRateLimit() {
        return -1;
    }

    static String getLatestVersion(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.getLatestVersion(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to check plugin '" + pluginData.getPluginName() + "' for latest version.");
        }
    }

    static String getDownloadUrl(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.getDownloadUrl(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to get download url for plugin '" + pluginData.getPluginName() + "'.");
        }
    }

    static boolean isUpdateAvailable(PluginData pluginData) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.isUpdateAvailable(pluginData, sourceData);
            });
        } catch (IOException e) {
            throw new IOException("Failed to check if update is available for plugin '" + pluginData.getPluginName() + "'.");
        }
    }

    static boolean download(PluginData pluginData, File destinationDir) throws IOException {
        try {
            return attemptOnSources(pluginData, (versionChecker, sourceData) -> {
                return versionChecker.download(pluginData, sourceData, destinationDir);
            });
        } catch (IOException e) {
            throw new IOException("Failed to download update for plugin '" + pluginData.getPluginName() + "'.");
        }
    }

    private static <T> T attemptOnSources(PluginData pluginData, SourceSupplier<T> callable) throws IOException {
        for (SourceData sourceData : pluginData.getSourceData()) {
            Source source = SourceRegistry.getSource(sourceData.name());
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
