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
import org.lushplugins.pluginupdater.api.util.StringUtil;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.comparator.BuildComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Optional;

public class JenkinsSource implements Source {
    public static final String NAME = "jenkins";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Version fetchLatestVersion(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException, InvalidVersionFormatException {
        if (!(sourceData instanceof Data jenkinsData)) {
            return null;
        }

        JsonObject buildJson = getLatestSuccessfulBuild(pluginData, jenkinsData);
        int buildNum = buildJson.get("number").getAsInt();

        return new Version(
            "b" + buildNum,
            null,
            null,
            buildNum,
            null,
            false
        );
    }

    @Override
    public DownloadableRelease fetchDownloadableRelease(PluginData pluginData, SourceData sourceData) throws IOException, InterruptedException {
        if (!(sourceData instanceof Data jenkinsData)) {
            return null;
        }

        JsonObject buildJson = getLatestSuccessfulBuild(pluginData, jenkinsData);

        JsonObject artifactJson = buildJson.get("artifacts").getAsJsonArray().asList().stream()
            .map(JsonElement::getAsJsonObject)
            .filter(artifact -> jenkinsData.artifactName()
                .map(filter -> StringUtil.matchesFilter(artifact.get("fileName").getAsString(), filter))
                .orElse(true))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Failed to find an artifact matching the artifact name format '%s'."
                .formatted(jenkinsData.artifactName())));

        String fileName = artifactJson.get("fileName").getAsString();
        return DownloadableRelease.builder()
            .pluginData(pluginData)
            .downloadUrl("%s/job/%s/lastSuccessfulBuild/artifact/artifacts/%s"
                .formatted(jenkinsData.url(), jenkinsData.job(), fileName))
            .jarName(fileName)
            .build();
    }

    public JsonObject getLatestSuccessfulBuild(PluginData pluginData, Data jenkinsData) throws IOException, InterruptedException {
        HttpResponse<String> response = HttpUtil.sendRequest("%s/job/%s/lastSuccessfulBuild/api/json"
            .formatted(jenkinsData.url(), jenkinsData.job()));

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Received invalid response code (" + response.statusCode() + ") whilst checking '" + pluginData.pluginName() + "' for updates.");
        }

        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    @Override
    public @Nullable String getChangelogUrl(PluginData pluginData, SourceData sourceData) {
        if (sourceData instanceof Data(String url, String job, var artifactName)) {
            return "%s/job/%s/changes"
                .formatted(url, job);
        }

        return null;
    }

    /**
     * @param url The plugin's Jenkins url (e.g. 'https://ci.jenkins.io')
     * @param job The Jenkins job
     * @param artifactName A string that the artifact name of the build must include
     */
    public record Data(String url, String job, Optional<String> artifactName) implements SourceData {

        @Override
        public String sourceName() {
            return NAME;
        }

        @Override
        public VersionComparator defaultComparator() {
            return BuildComparator.INSTANCE;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String url;
            private String job;
            private String artifactName;

            private Builder() {}

            public Builder url(String url) {
                this.url = url;
                return this;
            }

            public Builder job(String job) {
                this.job = job;
                return this;
            }

            public Builder artifactName(@Nullable String artifactName) {
                this.artifactName = artifactName;
                return this;
            }

            public Data build() {
                return new Data(
                    Objects.requireNonNull(url),
                    Objects.requireNonNull(job),
                    Optional.ofNullable(artifactName)
                );
            }
        }
    }
}
