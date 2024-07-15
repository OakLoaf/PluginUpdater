package org.lushplugins.pluginupdater.api.platform;

import org.lushplugins.pluginupdater.api.platform.github.GithubData;
import org.lushplugins.pluginupdater.api.platform.github.GithubVersionChecker;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarData;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarVersionChecker;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthVersionChecker;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotVersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionChecker;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.util.Pair;

import java.util.HashMap;
import java.util.concurrent.Callable;

public class PlatformRegistry {
    private static final HashMap<String, Pair<Callable<VersionChecker>, PlatformDataConstructor>> platformConstructors = new HashMap<>();
    private static final HashMap<String, VersionChecker> cachedPlatforms = new HashMap<>();

    static {
        register("github", GithubVersionChecker::new, GithubData::new);
        register("hangar", HangarVersionChecker::new, HangarData::new);
        register("modrinth", ModrinthVersionChecker::new, ModrinthData::new);
        register("spigot", SpigotVersionChecker::new, SpigotData::new);
    }

    @Nullable
    public static VersionChecker getVersionChecker(String platform) {
        return getOrConstructVersionChecker(platform);
    }

    @Nullable
    public static PlatformData getPlatformData(String platform, ConfigurationSection configurationSection) {
        try {
            return platformConstructors.containsKey(platform) ? platformConstructors.get(platform).second().apply(configurationSection) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void register(@NotNull String platform, @NotNull Callable<VersionChecker> updater, @NotNull PlatformRegistry.PlatformDataConstructor platformDataConstructor) {
        if (platformConstructors.containsKey(platform)) {
            throw new IllegalStateException("This platform is already registered");
        }

        platformConstructors.put(platform, new Pair<>(updater, platformDataConstructor));
    }

    public static void unregister(String platform) {
        platformConstructors.remove(platform);
    }

    private static @Nullable VersionChecker getOrConstructVersionChecker(@NotNull String platform) {
        if (cachedPlatforms.containsKey(platform)) {
            return cachedPlatforms.get(platform);
        } else if (platformConstructors.containsKey(platform)) {
            try {
                VersionChecker versionChecker = platformConstructors.get(platform).first().call();
                cachedPlatforms.put(platform, versionChecker);
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
