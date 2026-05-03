package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.updater.PluginData;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class ModrinthSource implements Source {

    @Override
    public String getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data modrinthData)) {
            return null;
        }

        JsonObject currVersionJson = getLatestVersion(pluginData, modrinthData);
        return currVersionJson.get("version_number").getAsString();
    }

    @Override
    public String getDownloadUrl(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data modrinthData)) {
            return null;
        }

        JsonObject currVersionJson = getLatestVersion(pluginData, modrinthData);
        return currVersionJson.get("files").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
    }

    private JsonArray getVersions(PluginData pluginData, Data modrinthData) throws IOException, InterruptedException {
        StringBuilder uriBuilder = new StringBuilder("%s/project/%s/version"
            .formatted(UpdaterConstants.Endpoint.MODRINTH, modrinthData.projectId()))
            .append("?loaders=[%22bukkit%22,%22spigot%22,%22paper%22,%22purpur%22,%22folia%22]")
            .append("&include_changelog=false");

        if (modrinthData.filtersReleaseChannel()) {
            uriBuilder.append("&version_type=").append(modrinthData.releaseChannel());
        }

        HttpResponse<String> response = HttpUtil.sendRequest(uriBuilder.toString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (%s) whilst checking '%s' for updates."
                .formatted(response.statusCode(), pluginData.getPluginName()));
        }

        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    private JsonObject getLatestVersion(PluginData pluginData, Data modrinthData) throws IOException, InterruptedException {
        JsonArray versions = getVersions(pluginData, modrinthData);
        if (versions.isEmpty()) {
            throw new IllegalStateException("Failed to collect versions for '%s'"
                .formatted(pluginData.getPluginName() ));
        }

        return versions.get(0).getAsJsonObject();
    }

    @Override
    public int getRateLimit() {
        return 1;
    }

    /**
     * @param projectId The Modrinth project id
     * @param releaseChannels Which release channels to filter, {@code null} will include all release channels
     */
    public record Data(String projectId, @Nullable List<String> releaseChannels) implements SourceData {

        /**
         * @param projectId The Modrinth project id
         * @param releaseChannel Which release channel to filter
         */
        public Data(String projectId, String releaseChannel) {
            this(projectId, Collections.singletonList(releaseChannel));
        }

        @Override
        public String name() {
            return "modrinth";
        }

        public boolean filtersReleaseChannel() {
            return this.releaseChannels != null;
        }

        @ApiStatus.Internal
        public @Nullable String releaseChannel() {
            return this.releaseChannels != null ? this.releaseChannels.getFirst() : null;
        }
    }

    public static class ReleaseChannel {
        public static final List<String> ALL = null;
        public static final String RELEASE = "release";
        public static final String BETA = "beta";
        public static final String ALPHA = "alpha";
    }
}
