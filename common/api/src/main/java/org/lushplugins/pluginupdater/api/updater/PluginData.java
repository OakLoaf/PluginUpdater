package org.lushplugins.pluginupdater.api.updater;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.exception.FailedAttemptsOnSourcesException;
import org.lushplugins.pluginupdater.api.exception.InvalidVersionFormatException;
import org.lushplugins.pluginupdater.api.source.*;
import org.lushplugins.pluginupdater.api.util.UpdaterConstants;
import org.lushplugins.pluginupdater.api.version.DownloadableRelease;
import org.lushplugins.pluginupdater.api.version.FetchedVersion;
import org.lushplugins.pluginupdater.api.version.Version;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.SemVerComparator;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;
import org.lushplugins.pluginupdater.api.version.parser.RegexVersionParser;
import org.lushplugins.pluginupdater.api.version.parser.VersionParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

public class PluginData {
    private final String pluginName;
    private final Version currentVersion;
    private final VersionParser latestVersionParser;
    private final List<SourceData> sourceData;
    private final VersionComparator comparator;
    private Version latestVersion;

    private boolean enabled = true;
    private VersionDifference versionDifference = VersionDifference.UNKNOWN;
    private final Collection<String> tags;
    private final boolean allowDownloads;
    private boolean alreadyDownloaded = false;
    private boolean checkRan = false;

    private PluginData(
        String pluginName,
        Version currentVersion,
        VersionParser latestVersionParser,
        List<SourceData> sourceData,
        @Nullable VersionComparator comparator,
        Collection<String> tags,
        boolean allowDownloads
    ) {
        this.pluginName = pluginName;
        this.currentVersion = currentVersion;
        this.latestVersionParser = latestVersionParser;
        this.sourceData = new ArrayList<>(sourceData);
        this.comparator = comparator;
        this.tags = tags;
        this.allowDownloads = allowDownloads;
    }

    public String pluginName() {
        return pluginName;
    }

    public Version currentVersion() {
        return currentVersion;
    }

    public VersionParser latestVersionParser() {
        return latestVersionParser;
    }

    public List<SourceData> sourceData() {
        return sourceData;
    }

    public void addSource(SourceData sourceData) {
        this.sourceData.add(sourceData);
    }

    public Optional<VersionComparator> versionComparator() {
        return Optional.ofNullable(comparator);
    }

    public Optional<Version> latestVersion() {
        return Optional.ofNullable(latestVersion);
    }

    public void latestVersion(Version latestVersion) {
        this.latestVersion = latestVersion;
    }

    public void latestVersion(String latestVersion) {
        latestVersion(this.latestVersionParser.parse(latestVersion));
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

    public VersionDifference versionDifference() {
        return versionDifference;
    }

    public void versionDifference(VersionDifference versionDifference) {
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

    public Optional<String> getChangelogUrl() {
        return this.sourceData.stream()
            .map(sourceData -> SourceRegistry.get(sourceData.sourceName())
                .flatMap(source -> Optional.ofNullable(source.getChangelogUrl(this, sourceData))))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();
    }

    public FetchedVersion fetchLatestVersion() throws InvalidVersionFormatException {
        try {
            return attemptOnSources((context) -> {
                Version version = context.source().fetchLatestVersion(this, context.sourceData());
                return new FetchedVersion(version, context);
            });
        } catch (FailedAttemptsOnSourcesException e) {
            throw new RuntimeException("Failed to fetch latest version for plugin '" + this.pluginName + "'.");
        }
    }

    public boolean checkForUpdate() {
        Version latestVersion;
        SourceContext sourceContext;
        try {
            FetchedVersion fetchedVersion = fetchLatestVersion();
            latestVersion = fetchedVersion.version();
            sourceContext = fetchedVersion.sourceContext();
        } catch (InvalidVersionFormatException e) {
            UpdaterConstants.LOGGER.severe("Failed to read latest version for '%s': %s".formatted(this.pluginName, e.getMessage()));
            return false;
        }

        VersionDifference versionDifference;
        try {
            VersionComparator comparator = versionComparator().orElse(sourceContext.sourceData().defaultComparator());
            versionDifference = comparator.compare(this.currentVersion, latestVersion);
        } catch (InvalidVersionFormatException e) {
            UpdaterConstants.LOGGER.severe("Failed to compare versions for '%s': %s".formatted(this.pluginName, e.getMessage()));
            return false;
        }

        this.checkRan = true;
        this.versionDifference = versionDifference;

        if (!versionDifference.equals(VersionDifference.LATEST)) {
            this.latestVersion = latestVersion;
            return true;
        } else {
            return false;
        }
    }

    public DownloadableRelease prepareDownloadableRelease() {
        try {
            return attemptOnSources((context) -> {
                return context.source().fetchDownloadableRelease(this, context.sourceData());
            });
        } catch (FailedAttemptsOnSourcesException e) {
            throw new RuntimeException("Failed to prepare downloadable release for plugin '" + this.pluginName + "'.");
        }
    }

    public boolean downloadUpdate(Path destinationDir) {
        try {
            prepareDownloadableRelease()
                .downloadTo(destinationDir);
            return true;
        } catch (IOException | InterruptedException e) {
            UpdaterConstants.LOGGER.log(Level.SEVERE, "Failed to download update for plugin '" + this.pluginName + "'.", e);
            return false;
        }
    }

    private <T> T attemptOnSources(SourceSupplier<T> supplier) throws FailedAttemptsOnSourcesException {
        for (SourceData sourceData : this.sourceData) {
            Source source = SourceRegistry.get(sourceData.sourceName()).orElse(null);
            if (source == null) {
                continue;
            }

            try {
                return supplier.apply(new SourceContext(source, sourceData));
            } catch (Throwable e) {
                UpdaterConstants.LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }
        }

        throw new FailedAttemptsOnSourcesException("Failed attempts on all available sources for plugin '" + this.pluginName + "'.");
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
        private VersionParser versionParser = RegexVersionParser.INSTANCE;
        private VersionParser latestVersionParser;
        private List<SourceData> sourceData = Collections.emptyList();
        private VersionComparator comparator = SemVerComparator.INSTANCE;
        private Collection<String> tags = Collections.emptyList();
        private boolean allowDownloads = true;

        private Builder(String pluginName, String currentVersion) {
            this.pluginName = pluginName;
            this.currentVersion = currentVersion;
        }

        public Builder versionParser(VersionParser versionParser) {
            this.versionParser = versionParser;
            return this;
        }

        public Builder latestVersionParser(VersionParser latestVersionParser) {
            this.latestVersionParser = latestVersionParser;
            return this;
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
            Version currentVersion;
            try {
                currentVersion = this.versionParser.parse(this.currentVersion);
            } catch (InvalidVersionFormatException e) {
                throw new RuntimeException("Failed to read current version for '%s': %s".formatted(this.pluginName, e.getMessage()));
            }

            return new PluginData(
                this.pluginName,
                currentVersion,
                this.latestVersionParser != null ? this.latestVersionParser : this.versionParser,
                this.sourceData,
                this.comparator,
                this.tags,
                this.allowDownloads
            );
        }
    }
}
