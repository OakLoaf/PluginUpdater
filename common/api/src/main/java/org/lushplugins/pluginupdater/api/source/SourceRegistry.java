package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.source.github.GithubData;
import org.lushplugins.pluginupdater.api.source.github.GithubVersionChecker;
import org.lushplugins.pluginupdater.api.source.hangar.HangarData;
import org.lushplugins.pluginupdater.api.source.hangar.HangarVersionChecker;
import org.lushplugins.pluginupdater.api.source.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.source.modrinth.ModrinthVersionChecker;
import org.lushplugins.pluginupdater.api.source.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.source.spigot.SpigotVersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionChecker;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class SourceRegistry {
    private static final HashMap<String, Source> sources = new HashMap<>();
    private static final HashMap<String, VersionChecker> cachedSourceCheckers = new HashMap<>();

    static {
        register("github", -1, GithubVersionChecker::new, GithubData::new);
        register("hangar", 1, HangarVersionChecker::new, HangarData::new);
        register("modrinth", 1, ModrinthVersionChecker::new, ModrinthData::new);
        register("spigot", 1, SpigotVersionChecker::new, SpigotData::new);
    }

    @Nullable
    public static VersionChecker getVersionChecker(String source) {
        return getOrConstructVersionChecker(source);
    }

    @Nullable
    public static SourceData getSourceData(String source, ConfigurationSection configurationSection) {
        try {
            return sources.containsKey(source) ? sources.get(source).sourceDataConstructor().apply(configurationSection) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void register(@NotNull String source, int rateLimit, @NotNull Callable<VersionChecker> updater, @NotNull SourceRegistry.SourceDataConstructor sourceDataConstructor) {
        if (sources.containsKey(source)) {
            throw new IllegalStateException("This source is already registered");
        }

        sources.put(source, new Source(rateLimit, updater, sourceDataConstructor));
    }

    public static void register(@NotNull String source, @NotNull Callable<VersionChecker> updater, @NotNull SourceRegistry.SourceDataConstructor sourceDataConstructor) {
        register(source, 1, updater, sourceDataConstructor);
    }

    public static void unregister(String source) {
        sources.remove(source);
    }

    private static @Nullable VersionChecker getOrConstructVersionChecker(@NotNull String source) {
        if (cachedSourceCheckers.containsKey(source)) {
            return cachedSourceCheckers.get(source);
        } else if (sources.containsKey(source)) {
            try {
                VersionChecker versionChecker = sources.get(source).updater().call();
                cachedSourceCheckers.put(source, versionChecker);
                return versionChecker;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @FunctionalInterface
    public interface SourceDataConstructor {
        SourceData apply(ConfigurationSection configurationSection);
    }
}
