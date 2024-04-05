package me.oak.pluginupdater.updater;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public abstract class PluginData {
    private final String pluginName;
    private final String platform;
    private final String currentVersion;
    private String latestVersion;

    private boolean enabled = true;
    private boolean updateAvailable = false;
    private boolean alreadyDownloaded = false;
    private boolean checkRan = false;

    public PluginData(@NotNull Plugin plugin, String platform, ConfigurationSection configurationSection) {
        this.pluginName = plugin.getName();
        this.platform = platform;

        String pluginVersion = plugin.getDescription().getVersion();
        Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(pluginVersion);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not find valid version format for '" + pluginName + "'");
        }
        this.currentVersion = matcher.group();
    }

    public PluginData(String pluginName, String platform, String currentVersion) {
        this.pluginName = pluginName;
        this.platform = platform;

        Matcher matcher = VersionChecker.VERSION_PATTERN.matcher(currentVersion);
        if (!matcher.find()) {
            throw new IllegalStateException("Could not find valid version format for '" + pluginName + "'");
        }
        this.currentVersion = matcher.group();
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getPlatform() {
        return platform;
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
        return updateAvailable;
    }

    public void setUpdateAvailable(boolean updateAvailable) {
        this.updateAvailable = updateAvailable;
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
