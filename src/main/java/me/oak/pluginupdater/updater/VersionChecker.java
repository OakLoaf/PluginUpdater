package me.oak.pluginupdater.updater;

import me.oak.pluginupdater.PluginUpdater;

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
        if (!VersionChecker.isLatestVersion(currentVersion, latestVersion)) {
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

    static boolean isLatestVersion(String currentVersionRaw, String newVersionRaw) {
        String[] currVersionParts = currentVersionRaw.split("\\.");
        String[] versionParts = newVersionRaw.split("\\.");

        int i = 0;
        for (String versionPart : versionParts) {
            if (i >= currVersionParts.length) {
                break;
            }

            int newVersion = Integer.parseInt(versionPart);
            int currVersion = Integer.parseInt(currVersionParts[i]);
            if (newVersion > currVersion) {
                if (i != 0) {
                    int newVersionLast = Integer.parseInt(versionParts[i-1]);
                    int currVersionLast = Integer.parseInt(currVersionParts[i-1]);
                    if (newVersionLast >= currVersionLast) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
            i++;
        }

        return true;
    }
}
