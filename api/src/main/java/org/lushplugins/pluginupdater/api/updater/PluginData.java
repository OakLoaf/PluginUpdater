package org.lushplugins.pluginupdater.api.updater;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.version.VersionDifference;
import org.lushplugins.pluginupdater.api.version.comparator.VersionComparator;

import java.util.List;
import java.util.Optional;

// TODO: Move from constructors to Builder class
public class PluginData {
    private final String pluginName;
    private final List<PlatformData> platformData;
    private final VersionComparator comparator;
    private final String currentVersion;
    private String latestVersion;

    private boolean enabled = true;
    private VersionDifference versionDifference = VersionDifference.UNKNOWN;
    private final boolean allowDownloads;
    private boolean alreadyDownloaded = false;
    private boolean checkRan = false;

    public PluginData(@NotNull String pluginName, @NotNull List<PlatformData> platformData, @Nullable VersionComparator comparator, @NotNull String currentVersion, boolean allowDownloads) {
        this.pluginName = pluginName;
        this.platformData = platformData;
        this.comparator = comparator;
        this.allowDownloads = allowDownloads;
        this.currentVersion = currentVersion;
    }

    public PluginData(@NotNull Plugin plugin, @NotNull List<PlatformData> platformData, boolean allowDownloads) {
        this(plugin.getName(), platformData, null, plugin.getDescription().getVersion(), allowDownloads);
    }

    public PluginData(@NotNull Plugin plugin, @NotNull PlatformData platformData, boolean allowDownloads) {
        this(plugin, List.of(platformData), allowDownloads);
    }

    public PluginData(@NotNull Plugin plugin, @NotNull List<PlatformData> platformData) {
        this(plugin, platformData, true);
    }

    public PluginData(@NotNull Plugin plugin, @NotNull PlatformData platformData) {
        this(plugin, List.of(platformData), true);
    }

    public String getPluginName() {
        return pluginName;
    }

    public List<PlatformData> getPlatformData() {
        return platformData;
    }

    public VersionComparator getComparator() {
        return comparator;
    }

    public Optional<VersionComparator> getOptionalComparator() {
        return Optional.ofNullable(comparator);
    }

    public void addPlatform(PlatformData platformData) {
        this.platformData.add(platformData);
    }

    // TODO: Ensure all uses do not get broken by version formatting not being applied here
    public String getCurrentVersion() {
        return currentVersion;
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

    public void setVersionDifference(@NotNull VersionDifference versionDifference) {
        this.versionDifference = versionDifference;
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
}
