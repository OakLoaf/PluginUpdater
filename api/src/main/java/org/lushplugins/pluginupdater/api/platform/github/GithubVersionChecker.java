package org.lushplugins.pluginupdater.api.platform.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.http.HttpResponse;

public class GithubVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof GithubData githubData)) {
            return null;
        }

        JsonObject releaseJson = getLatestRelease(pluginData, githubData);
        return releaseJson.get("tag_name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, PlatformData platformData) throws IOException, InterruptedException {
        if (!(platformData instanceof GithubData githubData)) {
            return null;
        }

        JsonObject releaseJson = getLatestRelease(pluginData, githubData);
        JsonArray assetsJson = releaseJson.get("assets").getAsJsonArray();
        return assetsJson.get(0).getAsJsonObject().get("browser_download_url").getAsString();
    }

    private JsonObject getLatestRelease(PluginData pluginData, GithubData githubData) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpUtil.sendRequest(String.format("%s/repos/%s/releases/latest", UpdaterConstants.APIs.GITHUB, githubData.getGithubRepo()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }
}
