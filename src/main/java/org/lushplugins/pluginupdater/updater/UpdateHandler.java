package org.lushplugins.pluginupdater.updater;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.logging.Level;

public class UpdateHandler {
    private final ScheduledExecutorService threads = Executors.newScheduledThreadPool(1);
    private final LinkedBlockingQueue<ProcessingData> queue = new LinkedBlockingQueue<>();

    public ScheduledExecutorService getThreads() {
        return threads;
    }

    public void enable() {
        threads.submit(() -> Thread.currentThread().setName("PluginUpdater Update Thread"));
        threads.scheduleAtFixedRate(this::processQueue, 15, 1, TimeUnit.SECONDS);
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

                try {
                    processingData.getFuture().complete(VersionChecker.isUpdateAvailable(pluginData));
                    pluginData.setCheckRan(true);
                    return;
                } catch (Exception e) {
                    PluginUpdater.getInstance().getLogger().log(Level.SEVERE, e.getMessage(), e);
                }

                String platformNames = String.join(", ", pluginData.getPlatformData().stream().map(PlatformData::getName).toList());
                processingData.getFuture().completeExceptionally(new IOException("Failed to run check for plugin '" + pluginData.getPluginName() + "' using defined platforms: '" + platformNames + "'"));
            }
            else if (processingData.getState().equals(ProcessingData.State.DOWNLOAD)) {
                PluginData pluginData = processingData.getPluginData();
                if (!pluginData.isEnabled() || !pluginData.isUpdateAvailable() || pluginData.isAlreadyDownloaded()) {
                    processingData.getFuture().complete(false);
                    return;
                }

                try {
                    if (VersionChecker.download(pluginData)) {
                        pluginData.setVersionDifference(VersionDifference.UNKNOWN);
                        pluginData.setAlreadyDownloaded(true);
                        processingData.getFuture().complete(true);
                    } else {
                        processingData.getFuture().complete(false);
                    }
                } catch (Exception e) {
                    processingData.getFuture().completeExceptionally(e);
                }

                String platformNames = String.join(", ", pluginData.getPlatformData().stream().map(PlatformData::getName).toList());
                processingData.getFuture().completeExceptionally(new IOException("Failed to download update for plugin '" + pluginData.getPluginName() + "' using defined platforms: '" + platformNames + "'"));
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
