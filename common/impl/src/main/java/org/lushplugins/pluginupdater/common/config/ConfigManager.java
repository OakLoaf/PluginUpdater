package org.lushplugins.pluginupdater.common.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.updater.PluginInfo;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;
import org.lushplugins.pluginupdater.common.config.deserializer.PluginDataDeserializer;
import org.lushplugins.pluginupdater.common.config.deserializer.SourceDataDeserializer;
import org.lushplugins.pluginupdater.common.UpdaterImpl;
import org.lushplugins.pluginupdater.common.updater.UpdateHandler;
import org.lushplugins.pluginupdater.common.util.ConfigUtil;

import java.util.*;
import java.util.logging.Level;

public class ConfigManager {
    private final UpdaterImpl updater;
    private boolean allowDownloads;
    private final Map<String, PluginData> plugins = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private final Set<String> disabledPlugins = new HashSet<>();
    private Messages messages;

    public ConfigManager(UpdaterImpl updater) {
        this.updater = updater;
        // TODO: Save default config if not there
    }

    public void reload() {
        // TODO: Access file location
        FileConfig config = FileConfig.of("");
        config.load();

        boolean checkOnReload = ConfigUtil.getOrAliasOrElse(
            config, "check-updates-on-reload", "check-updates-on-start", true,
            () -> updater.platform().getLogger().log(Level.WARNING, "Deprecated: The config section 'check-updates-on-start' has been renamed to 'check-updates-on-reload'")
        );

        this.allowDownloads = config.getOrElse("allow-downloads", true);
        this.messages = config.getOrElse("messages", () -> new Messages(new HashMap<>()));

        Collection<PluginData> dataSnapshot = new ArrayList<>(plugins.values());
        for (PluginData snapshot : dataSnapshot) {
            if (!snapshot.isAlreadyDownloaded()) {
                plugins.remove(snapshot.getPluginName());
            }
        }

        Config pluginsConfig = config.get("plugins");
        if (pluginsConfig != null) {
            pluginsConfig.entrySet().forEach(entry -> {
                String pluginName = entry.getKey();
                Config pluginConfig = entry.getRawValue();
                boolean enabled = pluginConfig.getOrElse("enabled", true);
                if (!enabled) {
                    disabledPlugins.add(pluginName);
                    return;
                }

                PluginData pluginData = PluginDataDeserializer.deserialize(updater, pluginName, pluginConfig);
                if (pluginData != null) {
                    addPlugin(pluginName, pluginData);
                }
            });
        }

        updater.collectUnknownPlugins().thenAccept(collectedPluginData -> {
            for (PluginData pluginData : collectedPluginData) {
                addPlugin(pluginData);
            }

            if (checkOnReload) {
                UpdateHandler updateHandler = updater.updateHandler();
                getPlugins().forEach(updateHandler::queueUpdateCheck);
            }
        });

        config.close();
    }

    public boolean shouldAllowDownloads() {
        return allowDownloads;
    }

    public boolean canRegisterPluginData(String pluginName) {
        return !plugins.containsKey(pluginName) && !disabledPlugins.contains(pluginName);
    }

    public Set<String> getPlugins() {
        return plugins.keySet();
    }

    public Collection<PluginData> getAllPluginData() {
        return plugins.values();
    }

    public @Nullable PluginData getPluginData(String pluginName) {
        return plugins.get(pluginName);
    }

    public void addPlugin(@NotNull PluginData pluginData) {
        addPlugin(pluginData.getPluginName(), pluginData);
    }

    public void addPlugin(String pluginName, @NotNull PluginData pluginData) {
        plugins.put(pluginName, pluginData);
    }

    public void removePlugin(String pluginName) {
        plugins.remove(pluginName);
    }

    public @Nullable String getMessage(String name) {
        return messages.get(name);
    }

    public String getMessage(String name, String def) {
        return messages.get(name, def);
    }


    public record Messages(Map<String, String> messages) {

        public String get(String key) {
            return messages.get(key);
        }

        public String get(String key, String def) {
            return messages.getOrDefault(key, def);
        }
    }
}
