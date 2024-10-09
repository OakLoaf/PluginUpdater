package org.lushplugins.pluginupdater.config;

import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.platform.PlatformRegistry;
import org.lushplugins.pluginupdater.api.platform.hangar.HangarData;
import org.lushplugins.pluginupdater.api.platform.modrinth.ModrinthData;
import org.lushplugins.pluginupdater.api.platform.spigot.SpigotData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class ConfigManager {
    private boolean checkOnStartup;
    private boolean allowDownloads;
    private final Map<String, PluginData> plugins = new TreeMap<>();
    private final HashMap<String, String> messages = new HashMap<>();

    public ConfigManager() {
        PluginUpdater.getInstance().saveDefaultConfig();
    }

    public void reloadConfig() {
        PluginUpdater plugin = PluginUpdater.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        checkOnStartup = config.getBoolean("check-updates-on-start", true);
        allowDownloads = config.getBoolean("allow-downloads", true);

        Collection<PluginData> dataSnapshot = new ArrayList<>(plugins.values());
        for (PluginData snapshot : dataSnapshot) {
            if (!snapshot.isAlreadyDownloaded()) {
                plugins.remove(snapshot.getPluginName());
            }
        }

        List<String> disabledPlugins = new ArrayList<>();
        ConfigurationSection pluginsSection = config.getConfigurationSection("plugins");
        if (pluginsSection != null) {
            getConfigurationSections(pluginsSection).forEach(pluginSection -> {
                String pluginName = pluginSection.getName();
                boolean enabled = pluginSection.getBoolean("enabled", true);
                String platform = pluginSection.getString("platform");
                if (!enabled) {
                    disabledPlugins.add(pluginName);
                    return;
                }
                else if (platform == null) {
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

        InputStream commonPluginsInputStream = PluginUpdater.getInstance().getResource("common-plugins.yml");
        YamlConfiguration commonPluginsYml = commonPluginsInputStream != null ? YamlConfiguration.loadConfiguration(new InputStreamReader(commonPluginsInputStream)) : null;
        for (Plugin plugin1 : PluginUpdater.getInstance().getServer().getPluginManager().getPlugins()) {
            String pluginName = plugin1.getName();
            if (disabledPlugins.contains(pluginName)) {
                continue;
            }

            InputStream pluginInputStream = plugin1.getResource("plugin.yml");
            if (pluginInputStream != null) {
                YamlConfiguration pluginYml = YamlConfiguration.loadConfiguration(new InputStreamReader(pluginInputStream));

                PlatformData platformData = null;
                if (pluginYml.contains("modrinth-project-id")) {
                    platformData = new ModrinthData(
                        pluginYml.getString("modrinth-project-id"),
                        true
                    );
                }
                else if (pluginYml.contains("spigot-resource-id")) {
                    platformData = new SpigotData(
                        pluginYml.getString("spigot-resource-id")
                    );
                }
                else if (pluginYml.contains("hangar-project-slug")) {
                    platformData = new HangarData(
                        pluginYml.getString("hangar-project-slug")
                    );
                }
                else if (commonPluginsYml != null && commonPluginsYml.contains(pluginName)) {
                    ConfigurationSection pluginSection = commonPluginsYml.getConfigurationSection(pluginName);
                    if (pluginSection != null) {
                        platformData = PlatformRegistry.getPlatformData(pluginSection.getString("platform"), pluginSection);
                    }
                }

                if (platformData != null) {
                    PluginData pluginData = new PluginData(plugin1, platformData);
                    addPlugin(pluginName, pluginData);
                }
            }
        }

        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            getConfigurationSections(messagesSection).forEach(messageSection -> {
                String messageName = messageSection.getName();
                setMessage(messageName, messagesSection.getString(messageName));
            });
        }

//        setMessage("updates-available", config.getString("messages.updates-available", "&#e0c01b%amount% &#ffe27aupdates are available, type &#e0c01b'%updates_command%' &#ffe27afor more information!"));
    }

    public boolean shouldCheckOnStartup() {
        return checkOnStartup;
    }

    public boolean shouldAllowDownloads() {
        return allowDownloads;
    }

    public Set<String> getPlugins() {
        return plugins.keySet();
    }

    public Collection<PluginData> getAllPluginData() {
        return plugins.values();
    }

    @Nullable
    public PluginData getPluginData(String pluginName) {
        return plugins.get(pluginName);
    }

    public void addPlugin(String pluginName, @NotNull PluginData pluginData) {
        plugins.put(pluginName, pluginData);
    }

    public void removePlugin(String pluginName) {
        plugins.remove(pluginName);
    }

    @Nullable
    public String getMessage(String name) {
        return getMessage(name, null);
    }

    @Nullable
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
