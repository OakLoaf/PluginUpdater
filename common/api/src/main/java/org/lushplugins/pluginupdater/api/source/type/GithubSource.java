package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GithubSource implements Source {
    public static final String NAME = "github";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data githubData)) {
            return null;
        }

        JsonObject releaseJson = getLatestRelease(pluginData, githubData);
        return releaseJson.get("tag_name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data githubData)) {
            return null;
        }

        JsonObject releaseJson = getLatestRelease(pluginData, githubData);
        JsonObject assetJson = releaseJson.get("assets").getAsJsonArray().get(0).getAsJsonObject();

        String token = githubData.getToken();
        if (token != null && !token.isEmpty()) {
            return assetJson.get("url").getAsString();
        }

        return assetJson.get("browser_download_url").getAsString();
    }

    @Override
    public Map<String, String> getDownloadHeaders(PluginData pluginData, SourceData sourceData) {
        if (!(sourceData instanceof Data githubData)) {
            return Collections.emptyMap();
        }

        String token = githubData.token();
        if (token == null || token.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("Accept", "application/octet-stream");
        return headers;
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

    public record Data(String repo, @Nullable String token) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }
    }
}
