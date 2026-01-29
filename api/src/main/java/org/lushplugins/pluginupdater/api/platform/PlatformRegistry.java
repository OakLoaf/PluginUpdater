package org.lushplugins.pluginupdater.api.platform;

import org.lushplugins.pluginupdater.api.platform.github.GithubData;
import org.lushplugins.pluginupdater.api.platform.github.GithubVersionChecker;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarData;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarVersionChecker;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthVersionChecker;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotVersionChecker;
import org.lushplugins.pluginupdater.api.platform.spigot.FancySpacesData;
import org.lushplugins.pluginupdater.api.platform.spigot.FancySpacesVersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionChecker;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class PlatformRegistry {
    private static final HashMap<String, Platform> platforms = new HashMap<>();
    private static final HashMap<String, VersionChecker> cachedPlatformCheckers = new HashMap<>();

    static {
        register("github", -1, GithubVersionChecker::new, GithubData::new);
        register("hangar", 1, HangarVersionChecker::new, HangarData::new);
        register("modrinth", 1, ModrinthVersionChecker::new, ModrinthData::new);
        register("spigot", 1, SpigotVersionChecker::new, SpigotData::new);
        register("fancy_spaces", 1, FancySpacesVersionChecker::new, FancySpacesData::new);
    }

    @Nullable
    public static VersionChecker getVersionChecker(String platform) {
        return getOrConstructVersionChecker(platform);
    }

    @Nullable
    public static PlatformData getPlatformData(String platform, ConfigurationSection configurationSection) {
        try {
            return platforms.containsKey(platform) ? platforms.get(platform).platformDataConstructor().apply(configurationSection) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void register(@NotNull String platform, int rateLimit, @NotNull Callable<VersionChecker> updater, @NotNull PlatformRegistry.PlatformDataConstructor platformDataConstructor) {
        if (platforms.containsKey(platform)) {
            throw new IllegalStateException("This platform is already registered");
        }

        platforms.put(platform, new Platform(rateLimit, updater, platformDataConstructor));
    }

    public static void register(@NotNull String platform, @NotNull Callable<VersionChecker> updater, @NotNull PlatformRegistry.PlatformDataConstructor platformDataConstructor) {
        register(platform, 1, updater, platformDataConstructor);
    }

    public static void unregister(String platform) {
        platforms.remove(platform);
    }

    private static @Nullable VersionChecker getOrConstructVersionChecker(@NotNull String platform) {
        if (cachedPlatformCheckers.containsKey(platform)) {
            return cachedPlatformCheckers.get(platform);
        } else if (platforms.containsKey(platform)) {
            try {
                VersionChecker versionChecker = platforms.get(platform).updater().call();
                cachedPlatformCheckers.put(platform, versionChecker);
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
    public interface PlatformDataConstructor {
        PlatformData apply(ConfigurationSection configurationSection);
    }
}
