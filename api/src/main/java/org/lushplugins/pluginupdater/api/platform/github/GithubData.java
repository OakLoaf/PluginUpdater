package org.lushplugins.pluginupdater.api.platform.github;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.pluginupdater.api.platform.PlatformData;

public class GithubData extends PlatformData {
    private static final String NAME = "github";

    private final String repo;

    public GithubData(ConfigurationSection config) {
        super(NAME);
        this.repo = config.getString("github-repo");
    }

    public GithubData(String repo) {
        super(NAME);
        this.repo = repo;
    }

    public String getRepo() {
        return repo;
    }
}
