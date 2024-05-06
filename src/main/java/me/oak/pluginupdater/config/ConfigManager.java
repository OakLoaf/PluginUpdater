package me.oak.pluginupdater.config;

import me.oak.pluginupdater.PluginUpdater;
import me.oak.pluginupdater.updater.PluginData;
import me.oak.pluginupdater.updater.platform.modrinth.ModrinthPluginData;
import me.oak.pluginupdater.updater.platform.spigot.SpigotPluginData;
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
    private final Map<String, PluginData> plugins = new TreeMap<>();
    private final HashMap<String, String> messages = new HashMap<>();

    public ConfigManager() {
        PluginUpdater.getInstance().saveDefaultConfig();
    }

    public void reloadConfig() {
        PluginUpdater plugin = PluginUpdater.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        checkOnStartup = config.getBoolean("check-updates-on-start");

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

                PluginData pluginData = PluginUpdater.getInstance().getPlatformRegistry().getPluginData(currPlugin, platform, pluginSection);
                if (pluginData != null) {
                    addPlugin(pluginName, pluginData);
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

                if (pluginYml.contains("modrinth-project-id")) {
                    String modrinthSlug = pluginYml.getString("modrinth-project-id");
                    addPlugin(pluginName, new ModrinthPluginData(pluginName, plugin1.getDescription().getVersion(), modrinthSlug, true));
                }
                else if (pluginYml.contains("spigot-resource-id")) {
                    String spigotResourceId = pluginYml.getString("spigot-resource-id");
                    addPlugin(pluginName, new SpigotPluginData(pluginName, plugin1.getDescription().getVersion(), spigotResourceId));
                }
                else if (commonPluginsYml != null && commonPluginsYml.contains(pluginName)) {
                    ConfigurationSection pluginSection = commonPluginsYml.getConfigurationSection(pluginName);
                    if (pluginSection != null) {
                        PluginData pluginData = PluginUpdater.getInstance().getPlatformRegistry().getPluginData(plugin1, pluginSection.getString("platform"), pluginSection);
                        if (pluginData != null) {
                            addPlugin(pluginName, pluginData);
                        }
                    }
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
