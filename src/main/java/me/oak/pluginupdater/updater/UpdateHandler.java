package me.oak.pluginupdater.updater;

import me.oak.pluginupdater.PluginUpdater;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.*;
import java.util.regex.Matcher;

public class UpdateHandler {
    private final ScheduledExecutorService threads = Executors.newScheduledThreadPool(1);
    private final LinkedBlockingQueue<ProcessingData> queue = new LinkedBlockingQueue<>();

    public void enable() {
        threads.submit(() -> Thread.currentThread().setName("PluginUpdater Update Thread"));
        threads.scheduleAtFixedRate(this::processQueue, 30, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean shutdown() {
        try {
            this.threads.shutdown();
            return threads.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ProcessingData queueUpdateCheck(String pluginName) {
        ProcessingData processingData = new ProcessingData(pluginName, ProcessingData.State.UPDATE_CHECK);
        queue(processingData);
        return processingData;
    }

    public ProcessingData queueDownload(String pluginName) {
        ProcessingData processingData = new ProcessingData(pluginName, ProcessingData.State.DOWNLOAD);
        queue(processingData);
        return processingData;
    }

    public void queue(ProcessingData processingData) {
        queue.add(processingData);
    }

    private void processQueue() {
        try {
            ProcessingData processingData = queue.take();
            if (processingData.getState().equals(ProcessingData.State.UPDATE_CHECK)) {
                PluginData pluginData = processingData.getPluginData();
                String platform = pluginData.getPlatform();

                VersionChecker versionChecker = PluginUpdater.getInstance().getPlatformRegistry().getUpdater(platform);
                if (versionChecker == null) {
                    return;
                }

                try {
                    String currentVersion = pluginData.getCurrentVersion();
                    Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(versionChecker.getLatestVersion(pluginData));
                    if (!matcher.find()) {
                        return;
                    }
                    String latestVersion = matcher.group();

                    if (!VersionChecker.isLatestVersion(currentVersion, latestVersion)) {
                        pluginData.setLatestVersion(latestVersion);
                        pluginData.setUpdateAvailable(true);
                        processingData.getFuture().complete(true);
                    } else {
                        processingData.getFuture().complete(false);
                    }

                    pluginData.setCheckRan(true);
                } catch (Exception e) {
                    if (e instanceof IllegalStateException) {
                        PluginUpdater.getInstance().getLogger().severe(e.getMessage());
                    } else {
                        processingData.getFuture().completeExceptionally(e);
                    }
                }
            }
            else if (processingData.getState().equals(ProcessingData.State.DOWNLOAD)) {
                PluginData pluginData = processingData.getPluginData();
                String platform = pluginData.getPlatform();
                if (!pluginData.isEnabled() || !pluginData.isUpdateAvailable() || pluginData.isAlreadyDownloaded()) {
                    return;
                }

                VersionChecker versionChecker = PluginUpdater.getInstance().getPlatformRegistry().getUpdater(platform);
                if (versionChecker == null) {
                    return;
                }

                String pluginName = pluginData.getPluginName();
                String latestVersion = pluginData.getLatestVersion();

                try {
                    String downloadUrl = versionChecker.getDownloadUrl(pluginData);
                    if (downloadUrl == null) {
                        return;
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

                    pluginData.setUpdateAvailable(false);
                    pluginData.setAlreadyDownloaded(true);
                    processingData.getFuture().complete(true);
                } catch (Exception e) {
                    processingData.getFuture().completeExceptionally(e);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class ProcessingData {
        private final String pluginName;
        private final State state;
        private final CompletableFuture<Boolean> future;

        public ProcessingData(String pluginName, State state) {
            this.pluginName = pluginName;
            this.state = state;
            this.future = new CompletableFuture<>();
        }

        public String getPluginName() {
            return pluginName;
        }

        public State getState() {
            return state;
        }

        public PluginData getPluginData() {
            return PluginUpdater.getInstance().getConfigManager().getPluginData(pluginName);
        }

        public CompletableFuture<Boolean> getFuture() {
            return future;
        }

        private enum State {
            UPDATE_CHECK,
            DOWNLOAD
        }
    }
}
