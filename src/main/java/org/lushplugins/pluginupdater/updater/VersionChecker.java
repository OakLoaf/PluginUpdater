package org.lushplugins.pluginupdater.updater;

import org.lushplugins.pluginupdater.PluginUpdater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface VersionChecker {
    Pattern VERSION_PATTERN = Pattern.compile("(\\d+(\\.\\d+)+)");

    String getLatestVersion(PluginData pluginData) throws IOException;

    String getDownloadUrl(PluginData pluginData) throws IOException;

    default boolean isUpdateAvailable(PluginData pluginData) throws IOException {
        String currentVersion = pluginData.getCurrentVersion();
        Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(getLatestVersion(pluginData));
        if (!matcher.find()) {
            return false;
        }
        String latestVersion = matcher.group();

        pluginData.setCheckRan(true);
        VersionDifference versionDifference = VersionChecker.getVersionDifference(currentVersion, latestVersion);
        if (!versionDifference.equals(VersionDifference.LATEST)) {
            pluginData.setLatestVersion(latestVersion);
            pluginData.setUpdateAvailable(true);
            return true;
        } else {
            return false;
        }
    }

    default boolean download(PluginData pluginData) throws IOException {
        String pluginName = pluginData.getPluginName();
        String latestVersion = pluginData.getLatestVersion();
        String downloadUrl = getDownloadUrl(pluginData);
        if (downloadUrl == null) {
            return false;
        }

        URL url = new URL(downloadUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());
        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Response code was " + connection.getResponseCode());
        }

        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        String fileName = pluginName + "-" + latestVersion + ".jar";
        File out = new File(PluginUpdater.getInstance().getUpdateFolder(), fileName);
        PluginUpdater.getInstance().getLogger().info("Saving '" + fileName + "' to '" + out.getAbsolutePath() + "'");
        FileOutputStream fos = new FileOutputStream(out);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();

        return true;
    }

    static VersionDifference getVersionDifference(String localVersionRaw, String releaseVersionRaw) {
        String[] localVersionParts = localVersionRaw.split("\\.");
        String[] releaseVersionParts = releaseVersionRaw.split("\\.");

        int i = 0;
        for (String releaseVersionPart : releaseVersionParts) {
            if (i >= localVersionParts.length) {
                break;
            }

            int newVersion = Integer.parseInt(releaseVersionPart);
            int localVersion = Integer.parseInt(localVersionParts[i]);
            if (newVersion > localVersion) {
                if (i != 0) {
                    int releaseVersionLast = Integer.parseInt(releaseVersionParts[i - 1]);
                    int localVersionLast = Integer.parseInt(localVersionParts[i - 1]);
                    if (releaseVersionLast >= localVersionLast) {
                        return i == 1
                            ? VersionDifference.MINOR : i == 2
                            ? VersionDifference.BUG_FIXES : VersionDifference.BUILD;
                    }
                } else {
                    return VersionDifference.MAJOR;
                }
            }
            i++;
        }

        return VersionDifference.LATEST;
    }
}
