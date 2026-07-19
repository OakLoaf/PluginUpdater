package org.lushplugins.pluginupdater.common.updater;

import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.common.UpdaterImpl;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;

public class UpdateHandler<T> {
    private final UpdaterImpl<T> updater;
    private final ScheduledExecutorService threads = Executors.newScheduledThreadPool(1);
    private final ArrayDeque<ProcessingData> queue = new ArrayDeque<>();
    private final Map<ProcessingData.State, Integer> currentlyProcessing = new HashMap<>();

    public UpdateHandler(UpdaterImpl<T> updater) {
        this.updater = updater;
    }

    public ScheduledExecutorService getThreads() {
        return threads;
    }

    public void enable() {
        this.threads.submit(() -> Thread.currentThread().setName("PluginUpdater Update Thread"));
        this.threads.scheduleAtFixedRate(this::processQueue, 0, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean shutdown() {
        try {
            this.threads.shutdown();
            return this.threads.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<ProcessingData.State, Integer> currentlyProcessing() {
        return currentlyProcessing;
    }

    public int remainingWithState(ProcessingData.State state) {
        return (int) this.queue.stream()
            .filter(data -> data.getState() == state)
            .count();
    }

    public ProcessingData queueUpdateCheck(String pluginName) {
        ProcessingData processingData = new ProcessingData(updater, pluginName, ProcessingData.State.UPDATE_CHECK);
        queue(processingData);
        return processingData;
    }

    public ProcessingData queueDownload(String pluginName) {
        ProcessingData processingData = new ProcessingData(updater, pluginName, ProcessingData.State.DOWNLOAD);
        queue(processingData);
        return processingData;
    }

    public ProcessingData queueBroadcastNotification() {
        ProcessingData processingData = new ProcessingData(updater, null, ProcessingData.State.SEND_NOTIFICATION);
        queue(processingData);
        return processingData;
    }

    public void queue(ProcessingData processingData) {
        queue.add(processingData);
    }

    private void processQueue() {
        ProcessingData processingData = queue.poll();
        if (processingData == null) {
            this.currentlyProcessing.clear();
            return;
        }

        ProcessingData.State state = processingData.getState();
        this.currentlyProcessing.compute(state, (key, oldValue) -> oldValue != null ? oldValue + 1 : 1);
        if (state != ProcessingData.State.SEND_NOTIFICATION) {
            List<T> users = updater.platform().getOnlineUsersWithPermission("pluginupdater.notify");
            if (users.isEmpty()) {
                return;
            }

            int processed = this.currentlyProcessing.getOrDefault(state, 1);
            int total = processed + this.remainingWithState(state);
            String message = "<#b7faa2>Updater processing: <#66b04f>%s<#b7faa2>/<#66b04f>%s"
                .formatted(processed, total);

            updater.platform().broadcastActionBar(users, message);
        }

        switch (state) {
            case UPDATE_CHECK -> {
                PluginData pluginData = processingData.getPluginData();

                try {
                    processingData.getFuture().complete(Source.isUpdateAvailable(pluginData));
                    pluginData.setCheckRan(true);
                    return;
                } catch (Exception e) {
                    updater.updaterPlugin().getLogger().log(Level.SEVERE, e.getMessage(), e);
                }

                String sourceNames = String.join(", ", pluginData.sourceData().stream().map(SourceData::sourceName).toList());
                processingData.getFuture().completeExceptionally(new IOException("Failed to run check for plugin '" + pluginData.pluginName() + "' using defined sources: '" + sourceNames + "'"));
            }
            case DOWNLOAD -> {
                PluginData pluginData = processingData.getPluginData();
                if (!pluginData.isEnabled() || !pluginData.isUpdateAvailable() || pluginData.isAlreadyDownloaded()) {
                    processingData.getFuture().complete(false);
                    return;
                }

                try {
                    if (Source.download(pluginData, updater.updaterPlugin().getDownloadDir())) {
                        pluginData.versionDifference(VersionDifference.UNKNOWN);
                        pluginData.setAlreadyDownloaded(true);
                        processingData.getFuture().complete(true);
                        return;
                    } else {
                        processingData.getFuture().complete(false);
                    }
                } catch (Exception e) {
                    processingData.getFuture().completeExceptionally(e);
                }

                String sourceNames = String.join(", ", pluginData.sourceData().stream().map(SourceData::sourceName).toList());
                processingData.getFuture().completeExceptionally(new IOException("Failed to download update for plugin '%s' using defined sources: '%s'".formatted(pluginData.pluginName(), sourceNames)));
            }
            case SEND_NOTIFICATION -> {
                String message = updater.constructUpdateMessage();
                if (message != null) {
                    List<T> users = updater.platform().getOnlineUsersWithPermission("pluginupdater.notify");
                    updater.platform().broadcastMessage(users, message);
                }
            }
        }
    }

    public static class ProcessingData {
        private final UpdaterImpl<?> updater;
        private final String pluginName;
        private final State state;
        private final CompletableFuture<Boolean> future;

        public ProcessingData(UpdaterImpl<?> updater, String pluginName, State state) {
            this.updater = updater;
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
            return updater.config().getPluginData(pluginName);
        }

        public CompletableFuture<Boolean> getFuture() {
            return future;
        }

        public enum State {
            UPDATE_CHECK,
            DOWNLOAD,
            SEND_NOTIFICATION
        }
    }
}
