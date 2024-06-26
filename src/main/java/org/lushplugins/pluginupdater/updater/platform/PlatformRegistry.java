package org.lushplugins.pluginupdater.updater.platform;

import org.lushplugins.pluginupdater.updater.platform.github.GithubPluginData;
import org.lushplugins.pluginupdater.updater.platform.github.GithubVersionChecker;
import org.lushplugins.pluginupdater.updater.platform.hangar.HangarPluginData;
import org.lushplugins.pluginupdater.updater.platform.hangar.HangarVersionChecker;
import org.lushplugins.pluginupdater.updater.platform.modrinth.ModrinthPluginData;
import org.lushplugins.pluginupdater.updater.PluginData;
import org.lushplugins.pluginupdater.updater.VersionChecker;
import org.lushplugins.pluginupdater.updater.platform.modrinth.ModrinthVersionChecker;
import org.lushplugins.pluginupdater.updater.platform.spigot.SpigotPluginData;
import org.lushplugins.pluginupdater.updater.platform.spigot.SpigotVersionChecker;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.util.Pair;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class PlatformRegistry {
    private final HashMap<String, Pair<Callable<VersionChecker>, PluginDataConstructor>> platforms = new HashMap<>();

    public PlatformRegistry() {
        register("github", GithubVersionChecker::new, GithubPluginData::new);
        register("hangar", HangarVersionChecker::new, HangarPluginData::new);
        register("modrinth", ModrinthVersionChecker::new, ModrinthPluginData::new);
        register("spigot", SpigotVersionChecker::new, SpigotPluginData::new);
    }

    @Nullable
    public VersionChecker getVersionChecker(String platform) {
        try {
            return platforms.containsKey(platform) ? platforms.get(platform).first().call() : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public PluginData getPluginData(Plugin plugin, String platform, ConfigurationSection configurationSection) {
        try {
            return platforms.containsKey(platform) ? platforms.get(platform).second().apply(plugin, configurationSection) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void register(@NotNull String platform, @NotNull Callable<VersionChecker> updater, @NotNull PluginDataConstructor pluginDataConstructor) {
        if (platforms.containsKey(platform)) {
            throw new IllegalStateException("This platform is already registered");
        }

        platforms.put(platform, new Pair<>(updater, pluginDataConstructor));
    }

    public void unregister(String platform) {
        platforms.remove(platform);
    }

    @FunctionalInterface
    public interface PluginDataConstructor {
        PluginData apply(Plugin plugin, ConfigurationSection configurationSection);
    }
}
