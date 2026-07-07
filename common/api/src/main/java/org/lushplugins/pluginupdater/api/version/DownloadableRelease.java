package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.util.BuildParameters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.Map;

public record DownloadableRelease(
    String downloadUrl,
    @Nullable Map<String, String> downloadHeaders,
    @Nullable String jarName
) {

    @Override
    public @NotNull Map<String, String> downloadHeaders() {
        return downloadHeaders != null ? downloadHeaders : Collections.emptyMap();
    }

    // TODO: Migrate to use modern HttpClient instead of relying on outdated HttpURLConnection
    public void downloadTo(File destinationDir, String fallbackFileName) throws IOException {
        URL url = URI.create(this.downloadUrl).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + BuildParameters.VERSION);
        for (Map.Entry<String, String> header : downloadHeaders().entrySet()) {
            connection.addRequestProperty(header.getKey(), header.getValue());
        }

        connection.setInstanceFollowRedirects(true);
        HttpURLConnection.setFollowRedirects(true);

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Response code was " + connection.getResponseCode());
        }

        // Get file name or default to PluginName-Version.jar
        String fileName = getFileName(connection, getFileName(connection, fallbackFileName));

        // Ensures update folder exists
        destinationDir.mkdirs();

        // Downloads file from url
        ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
        File out = new File(destinationDir, fileName);
        UpdaterConstants.LOGGER.info("Saving '" + fileName + "' to '" + out.getAbsolutePath() + "'");
        FileOutputStream fos = new FileOutputStream(out);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
    }

    private String getFileName(HttpURLConnection connection, String fallbackFileName) {
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            if (contentDisposition.contains("filename=")) {
                int index = contentDisposition.indexOf("filename=");
                String filename = contentDisposition.substring(index + 9); // Cut out 'filename='
                filename = filename.replace("\"", "").trim();

                // Strip any trailing parameters (like splitting at a semicolon)
                if (filename.contains(";")) {
                    filename = filename.substring(0, filename.indexOf(";"));
                }

                if (!filename.isEmpty()) {
                    return filename;
                }
            }
        }

        // Fallback to getting the file from the download URL
        int lastSlash = this.downloadUrl.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < this.downloadUrl.length() - 1) {
            String filename = this.downloadUrl.substring(lastSlash + 1);
            if (!filename.isEmpty()) {
                return filename;
            }
        }

        return fallbackFileName;
    }
}
