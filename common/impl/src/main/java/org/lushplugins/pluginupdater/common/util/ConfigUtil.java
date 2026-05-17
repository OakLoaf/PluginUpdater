package org.lushplugins.pluginupdater.common.util;

import com.electronwill.nightconfig.core.Config;

import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

public class ConfigUtil {

    public static <T> T getOrAlias(Config config, String path, String alternatePath) {
        return config.getOrElse(path, () -> config.get(alternatePath));
    }

    public static <T> T getOrAlias(Config config, String path, String alternatePath, Runnable ifAlias) {
        return config.getOrElse(path, () -> config.getOrElse(alternatePath, () -> {
            ifAlias.run();
            return null;
        }));
    }

    public static <T> T getOrAliasOrElse(Config config, String path, String alternatePath, Supplier<T> defaultValueSupplier) {
        T value = getOrAlias(config, path, alternatePath);
        return (value == null || value == NULL_OBJECT) ? defaultValueSupplier.get() : value;
    }

    public static <T> T getOrAliasOrElse(Config config, String path, String alternatePath, Supplier<T> defaultValueSupplier, Runnable ifAlias) {
        T value = getOrAlias(config, path, alternatePath, ifAlias);
        return (value == null || value == NULL_OBJECT) ? defaultValueSupplier.get() : value;
    }

    public static <T> T getOrAliasOrElse(Config config, String path, String alternatePath, T defaultValue) {
        T value = getOrAlias(config, path, alternatePath);
        return (value == null || value == NULL_OBJECT) ? defaultValue : value;
    }

    public static <T> T getOrAliasOrElse(Config config, String path, String alternatePath, T defaultValue, Runnable ifAlias) {
        T value = getOrAlias(config, path, alternatePath, ifAlias);
        return (value == null || value == NULL_OBJECT) ? defaultValue : value;
    }

    public static boolean isOfType(Config config, String path, Class<?> type) {
        return type.isInstance(config.get(path));
    }
}
