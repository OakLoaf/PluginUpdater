package org.lushplugins.pluginupdater.api.version.comparator;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ComparatorRegistry {
    private static final HashMap<String, VersionComparatorConstructor> comparators = new HashMap<>();

    static {
        register("build", BuildNumComparator::new);
        register("cal-ver", CalVerComparator::new);
        register("sem-ver", SemVerComparator::new);
    }

    public static @Nullable VersionComparator getVersionComparator(String id, ConfigurationSection config) {
        try {
            return comparators.containsKey(id) ? comparators.get(id).apply(config) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void register(@NotNull String id, VersionComparatorConstructor constructor) {
        if (comparators.containsKey(id)) {
            throw new IllegalStateException("This comparator is already registered");
        }

        comparators.put(id, constructor);
    }

    public static void unregister(String id) {
        comparators.remove(id);
    }

    @FunctionalInterface
    public interface VersionComparatorConstructor {
        VersionComparator apply(ConfigurationSection configurationSection);
    }
}
