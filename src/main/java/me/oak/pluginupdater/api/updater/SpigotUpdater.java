package me.oak.pluginupdater.api.updater;

import me.oak.pluginupdater.updater.platform.spigot.SpigotPluginData;
import me.oak.pluginupdater.updater.platform.spigot.SpigotVersionChecker;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SpigotUpdater extends Updater {

    private SpigotUpdater(@NotNull Plugin plugin, @NotNull String spigotResourceId) {
        super(plugin, new SpigotPluginData(plugin.getName(), plugin.getDescription().getVersion(), spigotResourceId), new SpigotVersionChecker());
    }

    public static class Builder {
        private final Plugin plugin;
        private final String spigotResourceId;

        public Builder(@NotNull Plugin plugin, @NotNull String spigotResourceId) {
            this.plugin = plugin;
            this.spigotResourceId = spigotResourceId;
        }

        public SpigotUpdater build() {
            return new SpigotUpdater(plugin, spigotResourceId);
        }
    }
}
