package org.lushplugins.pluginupdater.config;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.platform.PlatformRegistry;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.collector.PluginCollector;

import java.util.*;
import java.util.logging.Level;

public class ConfigManager {
    private boolean checkOnStartup;
    private boolean allowDownloads;
    private final Map<String, PluginData> plugins = new TreeMap<>();
    private final HashSet<String> disabledPlugins = new HashSet<>();
    private final HashMap<String, String> messages = new HashMap<>();

    public ConfigManager() {
        PluginUpdater.getInstance().saveDefaultConfig();
    }

    public void reloadConfig() {
        PluginUpdater plugin = PluginUpdater.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        checkOnStartup = config.getBoolean("check-updates-on-reload", true);
        allowDownloads = config.getBoolean("allow-downloads", true);

        if (config.contains("check-updates-on-start")) {
            checkOnStartup = config.getBoolean("check-updates-on-start", true);
            PluginUpdater.getInstance().getLogger().log(Level.WARNING, "Deprecated: The config section 'check-updates-on-start' has been renamed to 'check-updates-on-reload'");
        }

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            getConfigurationSections(messagesSection).forEach(messageSection -> {
                String messageName = messageSection.getName();
                setMessage(messageName, messagesSection.getString(messageName));
            });
        }

        Collection<PluginData> dataSnapshot = new ArrayList<>(plugins.values());
        for (PluginData snapshot : dataSnapshot) {
            if (!snapshot.isAlreadyDownloaded()) {
                plugins.remove(snapshot.getPluginName());
            }
        }

        ConfigurationSection pluginsSection = config.getConfigurationSection("plugins");
        if (pluginsSection != null) {
            getConfigurationSections(pluginsSection).forEach(pluginSection -> {
                String pluginName = pluginSection.getName();
                boolean enabled = pluginSection.getBoolean("enabled", true);
                String platform = pluginSection.getString("platform");

                if (!enabled) {
                    disabledPlugins.add(pluginName);
                    return;
                } else if (platform == null) {
                    return;
                }

                Plugin currPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
                if (currPlugin == null) {
                    return;
                }

                PlatformData platformData = PlatformRegistry.getPlatformData(platform, pluginSection);
                if (platformData != null) {
                    addPlugin(pluginName, new PluginData(currPlugin, platformData));
                }
            });
        }

        List<PluginData> collectedPluginData = PluginCollector.collectUnknownPlugins();
        for (PluginData pluginData : collectedPluginData) {
            addPlugin(pluginData);
        }
    }

    public boolean shouldCheckOnStartup() {
        return checkOnStartup;
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
        return getMessage(name, null);
    }

    public String getMessage(String name, String def) {
        return messages.getOrDefault(name, def);
    }

    public void setMessage(@NotNull String name, @NotNull String message) {
        messages.put(name, message);
    }

    public static List<ConfigurationSection> getConfigurationSections(ConfigurationSection configurationSection) {
        return configurationSection.getValues(false)
            .values()
            .stream()
            .filter(sectionRaw -> sectionRaw instanceof ConfigurationSection)
            .map(sectionRaw -> (ConfigurationSection) sectionRaw)
            .toList();
    }
}
