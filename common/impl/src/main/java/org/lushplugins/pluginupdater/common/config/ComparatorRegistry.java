package org.lushplugins.pluginupdater.common.config;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;
import org.lushplugins.pluginupdater.common.config.serializer.ComparatorDeserializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ComparatorRegistry {
    private static final Map<String, Function<Config, VersionComparator>> comparators = new HashMap<>();

    static {
        register("build", ComparatorDeserializer::buildNum);
        register("cal-ver", ComparatorDeserializer::calVer);
        register("sem-ver", ComparatorDeserializer::semVer);
    }

    public static @Nullable VersionComparator readVersionComparator(String id, Config config) {
        try {
            return comparators.containsKey(id) ? comparators.get(id).apply(config) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void register(@NotNull String id, Function<Config, VersionComparator> constructor) {
        if (comparators.containsKey(id)) {
            throw new IllegalStateException("This comparator is already registered");
        }

        comparators.put(id, constructor);
    }

    public static void unregister(String id) {
        comparators.remove(id);
    }
}
