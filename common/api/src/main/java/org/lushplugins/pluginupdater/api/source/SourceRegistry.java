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
        register(new GithubSource());
        register(new HangarSource());
        register(new SpigotSource());
    }

    public static @Nullable Source get(String name) {
        return sources.get(name);
    }

    public static void register(@NotNull Source source) {
        String name = source.getName();
        if (sources.containsKey(name)) {
            throw new IllegalStateException("A source with this name is already registered");
        }

        sources.put(name, source);
    }

    public static void unregister(String source) {
        sources.remove(source);
    }
}
