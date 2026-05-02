package org.lushplugins.pluginupdater.api.source.github;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;

public class GithubData extends SourceData {
    private static final String NAME = "github";

    private final String repo;
    private final String token;

    public GithubData(ConfigurationSection config) {
        super(NAME);
        this.repo = config.getString("github-repo");
        this.token = config.getString("token");
    }

    public GithubData(String repo, @Nullable String token) {
        super(NAME);
        this.repo = repo;
        this.token = token;
    }

    @Deprecated
    public GithubData(String repo) {
        this(repo, null);
    }

    public String getRepo() {
        return repo;
    }

    public String getToken() {
        return token;
    }
}
