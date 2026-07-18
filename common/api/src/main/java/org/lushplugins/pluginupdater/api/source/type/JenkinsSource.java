package org.lushplugins.pluginupdater.api.source.type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.source.Source;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.updater.PluginData;
import org.lushplugins.pluginupdater.api.util.HttpUtil;
import org.lushplugins.pluginupdater.api.util.StringComparison;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.comparator.BuildComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.IOException;
import java.net.http.HttpResponse;

public class JenkinsSource implements Source {
    public static final String NAME = "jenkins";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version getLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException, InvalidVersionFormatException {
        if (!(sourceData instanceof Data jenkinsData)) {
            return null;
        }

        JsonObject buildJson = getLatestSuccessfulBuild(pluginData, jenkinsData);
        int buildNum = buildJson.get("number").getAsInt();

        return new Version(
            "b" + buildNum,
            null,
            buildNum,
            null,
            false
        );
    }

    @Override
    public DownloadableRelease getDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data jenkinsData)) {
            return null;
        }

        JsonObject buildJson = getLatestSuccessfulBuild(pluginData, jenkinsData);

        JsonObject artifactJson = buildJson.get("artifacts").getAsJsonArray().asList().stream()
            .map(JsonElement::getAsJsonObject)
            .filter(artifact -> {
                String assetNameFilter = jenkinsData.artifactName();
                if (assetNameFilter == null) {
                    return true;
                }

                return StringComparison.matchesFilter(artifact.get("fileName").getAsString(), assetNameFilter);
            })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find an artifact matching the artifact name format '%s'."
                .formatted(jenkinsData.artifactName())));

        String fileName = artifactJson.get("fileName").getAsString();
        return new DownloadableRelease(
            "%s/job/%s/lastSuccessfulBuild/artifact/artifacts/%s"
                .formatted(jenkinsData.url(), jenkinsData.job(), fileName),
            null,
            fileName
        );
    }

    public JsonObject getLatestSuccessfulBuild(PluginData pluginData, Data jenkinsData) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpUtil.sendRequest("%s/job/%s/lastSuccessfulBuild/api/json"
            .formatted(jenkinsData.url(), jenkinsData.job()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.getPluginName() + "' for updates.");
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (sourceData instanceof Data(String url, String job, String artifactName)) {
            return "%s/job/%s/changes"
                .formatted(url, job);
        }

        return null;
    }

    public record Data(String url, String job, @Nullable String artifactName) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }

        @Override
        public VersionComparator getDefaultComparator() {
            return BuildComparator.INSTANCE;
        }
    }
}
