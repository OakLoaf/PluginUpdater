package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.StringFilter;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GithubSource implements Source {
    public static final String NAME = "github";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data githubData)) {
            return null;
        }

        JsonObject releaseJson = getLatestRelease(pluginData, githubData);
        String version = releaseJson.get("tag_name").getAsString();

        return pluginData.latestVersionParser().parse(version);
    }

    @Override
    public DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data githubData)) {
            return null;
        }

        JsonObject releaseJson = getLatestRelease(pluginData, githubData);
        JsonObject assetJson = releaseJson.get("assets").getAsJsonArray().asList().stream()
            .map(JsonElement::getAsJsonObject)
            .filter(asset -> githubData.assetName()
                .map(filter -> StringFilter.matchesFilter(asset.get("name").getAsString(), githubData.assetName().get()))
                .orElse(true))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find an asset matching the asset name format '%s'."
                .formatted(githubData.assetName())));

        String downloadUrl = githubData.token()
            .map(token -> assetJson.get("url").getAsString())
            .orElse(assetJson.get("browser_download_url").getAsString());

        Map<String, String> downloadHeaders = githubData.token()
            .map(token -> Map.of(
                    "Authorization", "Bearer " + token,
                    "Accept", "application/octet-stream"
                )
            )
            .orElse(null);

        return DownloadableRelease.builder(downloadUrl)
            .downloadHeaders(downloadHeaders)
            .build();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (sourceData instanceof Data(String repo, var token, var assetName)) {
            return "https://github.com/%s/releases"
                .formatted(repo);
        }

        return null;
    }

    private JsonObject getLatestRelease(PluginData pluginData, Data githubData) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpUtil.prepareRequestBuilder(URI.create("%s/repos/%s/releases/latest"
                .formatted(UpdaterConstants.Endpoint.GITHUB, githubData.repo())), null);

        githubData.token()
            .filter(token -> !token.isEmpty())
            .ifPresent(token -> requestBuilder.header("Authorization", "Bearer " + token));

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(
            requestBuilder.build(),
            HttpResponse.BodyHandlers.ofString());
        client.close();

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (%s) whilst checking '%s' for updates.".formatted(response.statusCode(), pluginData.pluginName()));
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    /**
     * @param repo The plugin's GitHub repo (e.g. 'OakLoaf/PluginUpdater')
     * @param token The GitHub access token (if required)
     * @param assetName A string that the asset name of the release must include
     */
    public record Data(String repo, Optional<String> token, Optional<String> assetName) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String repo;
            private String token;
            private String assetName;

            private Builder() {}

            public Builder repo(String repo) {
                this.repo = repo;
                return this;
            }

            public Builder token(@Nullable String token) {
                this.token = token;
                return this;
            }

            public Builder assetName(@Nullable String assetName) {
                this.assetName = assetName;
                return this;
            }

            public Data build() {
                return new Data(
                    Objects.requireNonNull(repo),
                    Optional.ofNullable(token),
                    Optional.ofNullable(assetName)
                );
            }
        }
    }
}
