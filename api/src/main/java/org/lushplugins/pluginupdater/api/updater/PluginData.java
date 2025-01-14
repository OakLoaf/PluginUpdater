package org.lushplugins.pluginupdater.api.updater;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pluginupdater.api.platform.PlatformData;
import org.lushplugins.pluginupdater.api.version.VersionChecker;
import org.lushplugins.pluginupdater.api.version.VersionDifference;

import java.util.List;
import java.util.regex.Matcher;

// TODO: Move from constructors to Builder class
public class PluginData {
    private final String pluginName;
    private final List<PlatformData> platformData;
    private final String currentVersion;
    private String latestVersion;

    private boolean enabled = true;
    private VersionDifference versionDifference = VersionDifference.UNKNOWN;
    private final boolean allowDownloads;
    private boolean alreadyDownloaded = false;
    private boolean checkRan = false;

    public PluginData(@NotNull Plugin plugin, @NotNull PlatformData platformData) {
        this(plugin, List.of(platformData), true);
    }

    public PluginData(@NotNull Plugin plugin, @NotNull PlatformData platformData, boolean allowDownloads) {
        this(plugin, List.of(platformData), allowDownloads);
    }

    public PluginData(@NotNull Plugin plugin, @NotNull List<PlatformData> platformData) {
        this(plugin, platformData, true);
    }

    public PluginData(@NotNull Plugin plugin, @NotNull List<PlatformData> platformData, boolean allowDownloads) {
        this.pluginName = plugin.getName();
        this.platformData = platformData;
        this.allowDownloads = allowDownloads;

        String pluginVersion = plugin.getDescription().getVersion();
        Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(pluginVersion);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not find valid version format for '" + pluginName + "'");
        }
        this.currentVersion = matcher.group();
    }

    public PluginData(@NotNull String pluginName, @NotNull PlatformData platformData, @NotNull String currentVersion) {
        this(pluginName, List.of(platformData), currentVersion, true);
    }

    public PluginData(@NotNull String pluginName, @NotNull PlatformData platformData, @NotNull String currentVersion, boolean allowDownloads) {
        this(pluginName, List.of(platformData), currentVersion, allowDownloads);
    }

    public PluginData(@NotNull String pluginName, @NotNull List<PlatformData> platformData, @NotNull String currentVersion) {
        this(pluginName, platformData, currentVersion, true);
    }

    public PluginData(@NotNull String pluginName, @NotNull List<PlatformData> platformData, @NotNull String currentVersion, boolean allowDownloads) {
        this.pluginName = pluginName;
        this.platformData = platformData;
        this.allowDownloads = allowDownloads;

        Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(currentVersion);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not find valid version format for '" + pluginName + "'");
        }
        this.currentVersion = matcher.group();
    }

    public String getPluginName() {
        return pluginName;
    }

    public List<PlatformData> getPlatformData() {
        return platformData;
    }

    public void addPlatform(PlatformData platformData) {
        this.platformData.add(platformData);
    }

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
