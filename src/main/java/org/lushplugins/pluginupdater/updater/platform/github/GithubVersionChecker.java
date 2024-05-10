package org.lushplugins.pluginupdater.updater.platform.github;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.lushplugins.pluginupdater.PluginUpdater;
import org.lushplugins.pluginupdater.updater.PluginData;
import org.lushplugins.pluginupdater.updater.VersionChecker;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubVersionChecker implements VersionChecker {

    @Override
    public String getLatestVersion(PluginData pluginData) throws IOException {
        if (!(pluginData instanceof GithubPluginData githubData)) {
            return null;
        }

        URL url = new URL("https://api.github.com/repos/" + githubData.getGithubRepo() + "/releases/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst getting the latest version for '" + pluginData.getPluginName() + "'.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonObject releaseJson = JsonParser.parseReader(reader).getAsJsonObject();
        return releaseJson.get("tag_name").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData) throws IOException {
        if (!(pluginData instanceof GithubPluginData githubData)) {
            return null;
        }

        URL url = new URL("https://api.github.com/repos/" + githubData.getGithubRepo() + "/releases/latest");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", "PluginUpdater/" + PluginUpdater.getInstance().getDescription().getVersion());

        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + connection.getResponseCode() + ") whilst getting the latest version for '" + pluginData.getPluginName() + "'.");
        }

        InputStream inputStream = connection.getInputStream();
        InputStreamReader reader = new InputStreamReader(inputStream);

        JsonObject releaseJson = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray assetsJson = releaseJson.get("assets").getAsJsonArray();
        return assetsJson.get(0).getAsJsonObject().get("browser_download_url").getAsString();
    }
}
