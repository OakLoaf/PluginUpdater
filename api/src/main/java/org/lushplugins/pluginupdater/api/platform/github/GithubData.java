package org.lushplugins.pluginupdater.api.platform.github;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class GithubData extends PlatformData {
    private static final String NAME = "github";

    private final String githubRepo;

    public GithubData(ConfigurationSection configurationSection) {
        super(NAME);
        this.githubRepo = configurationSection.getString("github-repo");
    }

    public GithubData(String githubRepo) {
        super(NAME);
        this.githubRepo = githubRepo;
    }

    public String getGithubRepo() {
        return githubRepo;
    }
}
