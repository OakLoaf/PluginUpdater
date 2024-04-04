package me.oak.pluginupdater.updater.platform;

import com.mojang.datafixers.util.Pair;
import me.oak.pluginupdater.updater.platform.modrinth.ModrinthPluginData;
import me.oak.pluginupdater.updater.PluginData;
import me.oak.pluginupdater.updater.VersionChecker;
import me.oak.pluginupdater.updater.platform.modrinth.ModrinthUpdater;
import me.oak.pluginupdater.updater.platform.spigot.SpigotPluginData;
import me.oak.pluginupdater.updater.platform.spigot.SpigotUpdater;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class PlatformRegistry {
    private final HashMap<String, Pair<Callable<VersionChecker>, PluginDataConstructor>> updaters = new HashMap<>();

    public PlatformRegistry() {
        register("modrinth", ModrinthUpdater::new, ModrinthPluginData::new);
        register("spigot", SpigotUpdater::new, SpigotPluginData::new);
    }

    @Nullable
    public VersionChecker getUpdater(String platform) {
        try {
            return updaters.containsKey(platform) ? updaters.get(platform).getFirst().call() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public PluginData getPluginData(Plugin plugin, String platform, ConfigurationSection configurationSection) {
        try {
            return updaters.containsKey(platform) ? updaters.get(platform).getSecond().apply(plugin, configurationSection) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void register(@NotNull String platform, @NotNull Callable<VersionChecker> updater, @NotNull PluginDataConstructor pluginDataConstructor) {
        if (updaters.containsKey(platform)) {
            throw new IllegalStateException("This platform is already registered");
        }

        updaters.put(platform, new Pair<>(updater, pluginDataConstructor));
    }

    public void unregister(String platform) {
        updaters.remove(platform);
    }

    @FunctionalInterface
    public interface PluginDataConstructor {
        PluginData apply(Plugin plugin, ConfigurationSection configurationSection);
    }
}
