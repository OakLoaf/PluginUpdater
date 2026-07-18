package org.lushplugins.pluginupdater.api.source;

import org.lushplugins.pluginupdater.api.source.type.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class SourceRegistry {
    private static final HashMap<String, Source> sources = new HashMap<>();

    static {
        register(new GithubSource());
        register(new HangarSource());
        register(new JenkinsSource());
        register(new SpigotSource());
    }

    public static Optional<Source> get(String name) {
        return Optional.ofNullable(sources.get(name));
    }

    public static Collection<Source> values() {
        return sources.values();
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
