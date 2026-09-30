package org.lushplugins.pluginupdater.common.notifier;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.config.deserializer.NotifierDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

public class NotifierRegistry {
    private static final Map<String, BiFunction<UpdaterImpl<?>, Config, Notifier>> notifiers = new HashMap<>();

    static {
        register("discord-webhook", NotifierDeserializer::discordWebHookNotifier);
    }

    public static @Nullable Notifier deserializeNotifier(UpdaterImpl<?> updater, String id, Config config) {
        if (config.get("enabled") == Boolean.FALSE) {
            return null;
        }

        try {
            return notifiers.containsKey(id) ? notifiers.get(id).apply(updater, config) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Notifier> deserializeNotifiers(UpdaterImpl<?> updater, Config config) {
        return config.entrySet().stream()
            .map(section -> {
                if (section.getValue() instanceof Config sectionConfig) {
                    String key = section.getKey();
                    if (notifiers.containsKey(key)) {
                        return deserializeNotifier(updater, key, sectionConfig);
                    } else {
                        String type = sectionConfig.get("type");
                        return deserializeNotifier(updater, type, sectionConfig);
                    }
                }

                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public static void register(@NotNull String id, BiFunction<UpdaterImpl<?>, Config, Notifier> constructor) {
        if (notifiers.containsKey(id)) {
            throw new IllegalStateException("This notifier is already registered");
        }

        notifiers.put(id, constructor);
    }

    public static void unregister(String id) {
        notifiers.remove(id);
    }
}
