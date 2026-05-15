package org.lushplugins.pluginupdater.api.updater;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.source.SourceData;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.util.*;

public class PluginData {
    private final String pluginName;
    private final String currentVersion;
    private final List<SourceData> sourceData;
    private final VersionComparator comparator;
    private String latestVersion;

    private boolean enabled = true;
    private VersionDifference versionDifference = VersionDifference.UNKNOWN;
    private final Collection<String> tags;
    private final boolean allowDownloads;
    private boolean alreadyDownloaded = false;
    private boolean checkRan = false;

    private PluginData(
        String pluginName,
        String currentVersion,
        List<SourceData> sourceData,
        @Nullable VersionComparator comparator,
        Collection<String> tags,
        boolean allowDownloads
    ) {
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.sourceData = sourceData;
        this.comparator = comparator;
        this.tags = tags;
        this.allowDownloads = allowDownloads;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public List<SourceData> getSourceData() {
        return sourceData;
    }

    public VersionComparator getComparator() {
        return comparator;
    }

    public Optional<VersionComparator> getOptionalComparator() {
        return Optional.ofNullable(comparator);
    }

    public void addSource(SourceData sourceData) {
        this.sourceData.add(sourceData);
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(String latestVersion) {
        this.latestVersion = latestVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isUpdateAvailable() {
        return !versionDifference.equals(VersionDifference.LATEST) && !versionDifference.equals(VersionDifference.UNKNOWN);
    }

    public VersionDifference getVersionDifference() {
        return versionDifference;
    }

    public void setVersionDifference(VersionDifference versionDifference) {
        this.versionDifference = versionDifference;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public Collection<String> getTags() {
        return tags;
    }

    public boolean areDownloadsAllowed() {
        return allowDownloads;
    }

    public boolean isAlreadyDownloaded() {
        return alreadyDownloaded;
    }

    public void setAlreadyDownloaded(boolean alreadyDownloaded) {
        this.alreadyDownloaded = alreadyDownloaded;
    }

    public boolean hasCheckRan() {
        return checkRan;
    }

    public void setCheckRan(boolean checkRan) {
        this.checkRan = checkRan;
    }

    public static Builder builder(String pluginName, String currentVersion) {
        return new Builder(pluginName, currentVersion);
    }

    public static Builder builder(PluginInfo plugin) {
        return builder(plugin.getName(), plugin.getVersion());
    }

    public static PluginData of(PluginInfo plugin) {
        return builder(plugin).build();
    }

    public static class Builder {
        private final String pluginName;
        private final String currentVersion;
        private List<SourceData> sourceData = Collections.emptyList();
        private VersionComparator comparator = SemVerComparator.INSTANCE;
        private Collection<String> tags = Collections.emptyList();
        private boolean allowDownloads = true;

        private Builder(String pluginName, String currentVersion) {
            this.pluginName = pluginName;
            this.currentVersion = currentVersion;
        }

        public Builder sourceData(SourceData sourceData) {
            this.sourceData = Collections.singletonList(sourceData);
            return this;
        }

        public Builder sourceData(List<SourceData> sourceData) {
            this.sourceData = sourceData;
            return this;
        }

        public Builder comparator(VersionComparator comparator) {
            this.comparator = comparator;
            return this;
        }

        public Builder tags(Collection<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder tag(String tag) {
            return tags(Collections.singletonList(tag));
        }

        public Builder allowDownloads(boolean allow) {
            this.allowDownloads = allow;
            return this;
        }

        public Builder allowDownloads() {
            return allowDownloads(true);
        }

        public Builder blockDownloads() {
            return allowDownloads(false);
        }

        public PluginData build() {
            return new PluginData(
                this.pluginName,
                this.currentVersion,
                this.sourceData,
                this.comparator,
                this.tags,
                this.allowDownloads
            );
        }
    }
}
