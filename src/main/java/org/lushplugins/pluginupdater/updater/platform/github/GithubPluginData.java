package org.lushplugins.pluginupdater.updater.platform.github;

import org.lushplugins.pluginupdater.updater.PluginData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class GithubPluginData extends PluginData {
    private final String githubRepo;

    public GithubPluginData(@NotNull Plugin plugin, ConfigurationSection configurationSection) {
        super(plugin, "github", configurationSection);
        this.githubRepo = configurationSection.getString("github-repo");
    }

    public GithubPluginData(String pluginName, String currentVersion, String githubRepo) {
        super(pluginName, "github", currentVersion);
        this.githubRepo = githubRepo;
    }

    public String getGithubRepo() {
        return githubRepo;
    }
}
