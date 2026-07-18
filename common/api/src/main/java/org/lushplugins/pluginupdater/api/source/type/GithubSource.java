package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.StringComparison;
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

        return pluginData.getLatestVersionParser().parse(version);
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
                .map(filter -> StringComparison.matchesFilter(asset.get("name").getAsString(), githubData.assetName().get()))
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

        return DownloadableRelease.builder()
            .downloadUrl(downloadUrl)
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
            throw new IllegalStateException("Received invalid response code (%s) whilst checking '%s' for updates.".formatted(response.statusCode(), pluginData.getPluginName()));
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public record Data(String repo, Optional<String> token, Optional<String> assetName) implements SourceData {

        public Data(String repo, @Nullable String token, @Nullable String assetName) {
            this(repo, Optional.ofNullable(token), Optional.ofNullable(assetName));
        }

        @Override
        public String sourceName() {
            return NAME;
        }
    }
}
