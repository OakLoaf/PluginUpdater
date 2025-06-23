package org.lushplugins.pluginupdater.updater;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;

public class UpdateHandler {
    private final ScheduledExecutorService threads = Executors.newScheduledThreadPool(1);
    private final ArrayDeque<ProcessingData> queue = new ArrayDeque<>();
    private final Map<ProcessingData.State, Integer> currentlyProcessing = new HashMap<>();

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

    public int remainingWithState(ProcessingData.State state) {
        return (int) this.queue.stream()
            .filter(data -> data.getState() == state)
            .count();
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
        ProcessingData processingData = queue.poll();
        if (processingData == null) {
            this.currentlyProcessing.clear();
            return;
        }

        ProcessingData.State state = processingData.getState();
        sendNotification(state);
        switch (state) {
            case UPDATE_CHECK -> {
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
            case DOWNLOAD -> {
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
                        return;
                    } else {
                        processingData.getFuture().complete(false);
                    }
                } catch (Exception e) {
                    processingData.getFuture().completeExceptionally(e);
                }

                String platformNames = String.join(", ", pluginData.getPlatformData().stream().map(PlatformData::getName).toList());
                processingData.getFuture().completeExceptionally(new IOException("Failed to download update for plugin '%s' using defined platforms: '%s'".formatted(pluginData.getPluginName(), platformNames)));
            }
        }
    }

    public void sendNotification(ProcessingData.State state) {
        Player[] players = Bukkit.getOnlinePlayers().stream()
            .filter(player -> player.hasPermission("pluginupdater.notify"))
            .toArray(Player[]::new);

        if (players.length == 0) {
            return;
        }

        int processed = this.currentlyProcessing.compute(state, (key, oldValue) -> oldValue != null ? oldValue + 1 : 1);
        int total = processed + remainingWithState(state);

        ChatColorHandler.sendActionBarMessage(players, "&#b7faa2Updater processing: &#66b04f%s&#b7faa2/&#66b04f%s"
            .formatted(processed, total));
    }

    public static class ProcessingData {
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

        public enum State {
            UPDATE_CHECK,
            DOWNLOAD
        }
    }
}
