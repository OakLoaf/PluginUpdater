package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.source.type.GithubSource;
import org.lushplugins.pluginupdater.api.source.type.HangarSource;
import org.lushplugins.pluginupdater.api.source.type.ModrinthSource;
import org.lushplugins.pluginupdater.api.source.type.SpigotSource;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class SourceRegistry {
    private static final HashMap<String, Source> sources = new HashMap<>();

    static {
        register("github", new GithubSource());
        register("hangar", new HangarSource());
        register("modrinth", new ModrinthSource());
        register("spigot", new SpigotSource());
    }

    @Nullable
    public static Source getSource(String name) {
        return sources.get(name);
    }

    @Nullable
    public static SourceData getSourceData(String source, Config configurationSection) {
        try {
            return sources.containsKey(source) ? sources.get(source).sourceDataConstructor().apply(configurationSection) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void register(@NotNull String name, @NotNull Source source) {
        if (sources.containsKey(name)) {
            throw new IllegalStateException("A source with this name is already registered");
        }

        sources.put(name, source);
    }

    public static void unregister(String source) {
        sources.remove(source);
    }
}
