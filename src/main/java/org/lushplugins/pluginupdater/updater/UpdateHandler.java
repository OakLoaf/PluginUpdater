package org.lushplugins.pluginupdater.updater;

import org.lushplugins.pluginupdater.PluginUpdater;

import java.util.concurrent.*;

public class UpdateHandler {
    private final ScheduledExecutorService threads = Executors.newScheduledThreadPool(1);
    private final LinkedBlockingQueue<ProcessingData> queue = new LinkedBlockingQueue<>();

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
                String platform = pluginData.getPlatform();

                VersionChecker versionChecker = PluginUpdater.getInstance().getPlatformRegistry().getVersionChecker(platform);
                if (versionChecker == null) {
                    processingData.getFuture().complete(false);
                    return;
                }

                try {
                    processingData.getFuture().complete(versionChecker.isUpdateAvailable(pluginData));
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
                    processingData.getFuture().complete(false);
                    return;
                }

                VersionChecker versionChecker = PluginUpdater.getInstance().getPlatformRegistry().getVersionChecker(platform);
                if (versionChecker == null) {
                    processingData.getFuture().complete(false);
                    return;
                }

                try {
                    if (versionChecker.download(pluginData)) {
                        pluginData.setUpdateAvailable(false);
                        pluginData.setAlreadyDownloaded(true);
                        processingData.getFuture().complete(true);
                    } else {
                        processingData.getFuture().complete(false);
                    }
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
