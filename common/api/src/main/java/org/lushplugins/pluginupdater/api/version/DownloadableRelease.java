package org.lushplugins.pluginupdater.api.version;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.DownloadLogger;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.util.BuildParameters;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record DownloadableRelease(
    PluginData pluginData,
    String downloadUrl,
    Map<String, String> downloadHeaders,
    Optional<String> jarName
) {

    public void downloadTo(Path destinationDir) throws IOException, InterruptedException {
        String version = pluginData.latestVersion().orElseThrow().resolvedVersion();
        String fallbackFileName = pluginData.pluginName() + "-" + version + ".jar";

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(this.downloadUrl))
            .header("User-Agent", "PluginUpdater/" + BuildParameters.VERSION);

        this.downloadHeaders.forEach(requestBuilder::header);

        HttpRequest request = requestBuilder
            .GET()
            .build();

        HttpClient client = HttpUtil.client();
        HttpResponse<Void> response = client.send(
            request,
            HttpResponse.BodyHandlers.discarding()
        );

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Response code was " + response.statusCode());
        }

        // Get file name or default to PluginName-Version.jar
        String fileName = this.jarName.orElseGet(() -> getFileName(response, fallbackFileName));
        Path downloadPath = destinationDir.resolve(fileName);
        // Ensures update folder exists
        Files.createDirectories(downloadPath.getParent());

        UpdaterConstants.LOGGER.info("Saving '" + fileName + "' to '" + downloadPath.toAbsolutePath() + "'");
        HttpResponse<Path> downloadResponse = client.send(
            request,
            HttpResponse.BodyHandlers.ofFile(downloadPath)
        );

        if (downloadResponse.statusCode() != 200) {
            throw new IllegalStateException("Download response code was " + response.statusCode());
        }

        DownloadLogger.logDownload(pluginData);
    }

    private String getFileName(HttpResponse<?> response, String fallbackFileName) {
        String contentDisposition = response.headers().firstValue("Content-Disposition").orElse(null);
        if (contentDisposition != null && !contentDisposition.isEmpty()) {
            if (contentDisposition.contains("filename=")) {
                int index = contentDisposition.indexOf("filename=");
                String filename = contentDisposition.substring(index + 9); // Cut out 'filename='
                filename = filename.replace("\"", "").trim();

                // Strip any trailing parameters (like splitting at a semicolon)
                if (filename.contains(";")) {
                    filename = filename.substring(0, filename.indexOf(";"));
                }

                if (filename.endsWith(".jar")) {
                    return filename.replace("%20", " ");
                }
            }
        }

        // Fallback to getting the file from the download URL
        int lastSlash = this.downloadUrl.lastIndexOf('/');
        if (lastSlash != -1 && lastSlash < this.downloadUrl.length() - 1) {
            String filename = this.downloadUrl.substring(lastSlash + 1);
            if (filename.endsWith(".jar")) {
                return filename.replace("%20", " ");
            }
        }

        return fallbackFileName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PluginData pluginData;
        private String downloadUrl;
        private Map<String, String> downloadHeaders;
        private String jarName;

        private Builder() {}

        public Builder pluginData(PluginData pluginData) {
            this.pluginData = pluginData;
            return this;
        }

        public Builder downloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder downloadHeaders(@Nullable Map<String, String> downloadHeaders) {
            this.downloadHeaders = downloadHeaders;
            return this;
        }

        public Builder jarName(@Nullable String jarName) {
            this.jarName = jarName;
            return this;
        }

        public DownloadableRelease build() {
            return new DownloadableRelease(
                Objects.requireNonNull(pluginData),
                Objects.requireNonNull(downloadUrl),
                downloadHeaders != null ? downloadHeaders : Collections.emptyMap(),
                Optional.ofNullable(jarName)
            );
        }
    }
}
