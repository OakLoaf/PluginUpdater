package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

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
            .filter(asset -> {
                String assetNameFilter = githubData.assetName();
                if (assetNameFilter == null) {
                    return true;
                }

                String assetName = asset.get("name").getAsString();
                char filterModifier = assetNameFilter.charAt(0);
                return switch (filterModifier) {
                    // Asset name is equal to string
                    case '=' -> assetName.equalsIgnoreCase(assetNameFilter.substring(1));
                    // Asset name does not contain string
                    case '!' -> !assetName.contains(assetNameFilter.substring(1));
                    // String matches regex
                    case '?' -> {
                        Pattern pattern = Pattern.compile(assetNameFilter.substring(1));
                        yield pattern.matcher(assetName).find();
                    }
                    // String contains
                    default -> assetName.contains(assetNameFilter);
                };
            })
            .findFirst()
            .orElse(null);

        if (assetJson == null) {
            throw new IllegalStateException("Failed to find an asset matching the asset name format '%s'.".formatted(githubData.assetName()));
        }

        String token = githubData.token();
        String downloadUrl;
        Map<String, String> downloadHeaders;
        if (token != null && !token.isEmpty()) {
            downloadUrl = assetJson.get("url").getAsString();

            downloadHeaders = new HashMap<>();
            downloadHeaders.put("Authorization", "Bearer " + token);
            downloadHeaders.put("Accept", "application/octet-stream");
        } else {
            downloadUrl = assetJson.get("browser_download_url").getAsString();
            downloadHeaders = null;
        }

        return new DownloadableRelease(downloadUrl, downloadHeaders, null);
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data(String repo, String token, String assetName))) {
            return null;
        }

        return "https://github.com/%s/releases"
            .formatted(repo);
    }

    private JsonObject getLatestRelease(PluginData pluginData, Data githubData) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpUtil.prepareRequestBuilder(URI.create("%s/repos/%s/releases/latest"
                .formatted(UpdaterConstants.Endpoint.GITHUB, githubData.repo())), null);

        String token = githubData.token();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

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

    public record Data(String repo, @Nullable String token, String assetName) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }
    }
}
