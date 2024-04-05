package me.oak.pluginupdater.api.updater;

import me.oak.pluginupdater.updater.platform.modrinth.ModrinthPluginData;
import me.oak.pluginupdater.updater.platform.modrinth.ModrinthVersionChecker;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class ModrinthUpdater extends Updater {

    private ModrinthUpdater(@NotNull Plugin plugin, @NotNull String modrinthProjectSlug, boolean featuredOnly) {
        super(plugin, new ModrinthPluginData(plugin.getName(), plugin.getDescription().getVersion(), modrinthProjectSlug, featuredOnly), new ModrinthVersionChecker());
    }

    public static class Builder {
        private final Plugin plugin;
        private final String modrinthProjectSlug;
        private boolean featuredOnly = false;

        public Builder(@NotNull Plugin plugin, @NotNull String modrinthProjectSlug) {
            this.plugin = plugin;
            this.modrinthProjectSlug = modrinthProjectSlug;
        }

        public Builder setFeaturedOnly(boolean featuredOnly) {
            this.featuredOnly = featuredOnly;
            return this;
        }

        public ModrinthUpdater build() {
            return new ModrinthUpdater(plugin, modrinthProjectSlug, featuredOnly);
        }
    }
}
